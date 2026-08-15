import { useState, useEffect, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { restApi } from '../api';
import { useAuthStore } from '../store';
import { Client } from '@stomp/stompjs';
import { OpenVidu, Publisher, Subscriber, Session as OVSession } from 'openvidu-browser';
import { Send, ArrowLeft, Mic, MicOff, Video, VideoOff } from 'lucide-react';

const GRAPHQL_URL = '/graphql';

const gqlFetch = async (query: string, variables?: Record<string, any>) => {
  const token = localStorage.getItem('token');
  const res = await fetch(GRAPHQL_URL, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: JSON.stringify({ query, variables }),
  });
  const json = await res.json();
  if (json.errors) {
    throw new Error(json.errors.map((e: any) => e.message).join(', '));
  }
  return json.data;
};

const GET_MESSAGES_QUERY = `
  query GetMessages($roomId: ID!) {
    messages(roomId: $roomId, page: 0, size: 50) {
      id
      content
      sentAt
      senderUsername
    }
  }
`;

export default function Room() {
  const { id: roomId } = useParams();
  const navigate = useNavigate();
  const user = useAuthStore(state => state.user);
  const token = useAuthStore(state => state.token);

  // Chat State
  const [messages, setMessages] = useState<any[]>([]);
  const [chatInput, setChatInput] = useState('');
  const stompClient = useRef<Client | null>(null);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  // Video State
  const [videoError, setVideoError] = useState<string | null>(null);
  const [videoJoined, setVideoJoined] = useState(false);
  const [session, setSession] = useState<OVSession | null>(null);
  const [publisher, setPublisher] = useState<Publisher | null>(null);
  const [subscribers, setSubscribers] = useState<Subscriber[]>([]);
  const [audioEnabled, setAudioEnabled] = useState(true);
  const [videoEnabled, setVideoEnabled] = useState(true);

  useEffect(() => {
    if (!roomId || !user) return;
    
    fetchPreviousMessages();
    connectChat();

    return () => {
      if (stompClient.current) {
        stompClient.current.deactivate();
      }
      leaveVideoSession();
    };
  }, [roomId, user]);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  // --- CHAT LOGIC ---
  const fetchPreviousMessages = async () => {
    try {
      const data = await gqlFetch(GET_MESSAGES_QUERY, { roomId });
      const msgs = Array.isArray(data.messages) ? data.messages : [];
      setMessages(msgs.slice().reverse()); // Chronological order
    } catch (err) {
      console.error('Failed to fetch messages', err);
    }
  };

  const connectChat = () => {
    const wsProtocol = window.location.protocol === 'https:' ? 'wss' : 'ws';
    const client = new Client({
      brokerURL: `${wsProtocol}://${window.location.host}/ws/raw`,
      connectHeaders: { Authorization: `Bearer ${token}` },
      debug: function (str) { console.log('[STOMP]', str); },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });

    client.onConnect = () => {
      console.log('STOMP Connected!');
      client.subscribe(`/topic/chat/${roomId}`, (msg) => {
        const newMsg = JSON.parse(msg.body);
        setMessages(prev => [...prev, {
          id: Date.now().toString(),
          content: newMsg.content,
          senderUsername: newMsg.from,
          sentAt: newMsg.timestamp || new Date().toISOString()
        }]);
      });
    };

    client.onStompError = (frame) => {
      console.error('STOMP error:', frame.headers['message'], frame.body);
    };

    client.onWebSocketError = (event) => {
      console.error('WebSocket error:', event);
    };

    client.activate();
    stompClient.current = client;
  };

  const sendMessage = () => {
    if (!chatInput.trim()) return;
    
    if (!stompClient.current?.connected) {
      alert("Chat not connected yet. Please wait...");
      return;
    }
    
    stompClient.current.publish({
      destination: `/app/chat/${roomId}`,
      body: JSON.stringify({ 
        from: user?.username, 
        content: chatInput 
      })
    });
    setChatInput('');
  };

  // --- VIDEO LOGIC (OpenVidu) ---
  const joinVideoSession = async () => {
    try {
      setVideoError(null);
      
      const OV = new OpenVidu();
      // Enable detailed logs for debugging OpenVidu
      OV.enableProdMode();
      
      const mySession = OV.initSession();

      mySession.on('streamCreated', (event) => {
        const subscriber = mySession.subscribe(event.stream, undefined);
        setSubscribers(prev => [...prev, subscriber]);
      });

      mySession.on('streamDestroyed', (event) => {
        setSubscribers(prev => prev.filter(sub => sub !== event.stream.streamManager));
      });

      mySession.on('exception', (exception) => {
        console.warn('OpenVidu exception:', exception);
      });

      // 1. Initialize publisher to request camera permissions
      const pub = await OV.initPublisherAsync(undefined, {
        audioSource: undefined,
        videoSource: undefined,
        publishAudio: true,
        publishVideo: true,
        resolution: '640x480',
        frameRate: 30,
        insertMode: 'APPEND',
        mirror: true
      });

      // 2. Fetch connection token from backend
      console.log('Requesting media session for room:', roomId);
      const sessionData = await restApi.createMediaSession(roomId!);
      console.log('Media session created/fetched:', sessionData);
      
      const tokenData = await restApi.generateMediaToken(sessionData.sessionId);
      console.log('Media token fetched:', tokenData.token);

      // 3. Connect to the session
      await mySession.connect(tokenData.token, { clientData: user?.username });

      // 4. Publish our stream
      await mySession.publish(pub);
      
      setSession(mySession);
      setPublisher(pub);
      setVideoJoined(true);

    } catch (err: any) {
      console.error('Error joining video session:', err);
      setVideoError(err.message || 'Failed to access camera/microphone or connect to media server');
    }
  };

  const leaveVideoSession = () => {
    if (session) {
      session.disconnect();
    }
    setSession(null);
    setPublisher(null);
    setSubscribers([]);
    setVideoJoined(false);
  };

  const toggleAudio = () => {
    if (publisher) {
      const newState = !audioEnabled;
      publisher.publishAudio(newState);
      setAudioEnabled(newState);
    }
  };

  const toggleVideo = () => {
    if (publisher) {
      const newState = !videoEnabled;
      publisher.publishVideo(newState);
      setVideoEnabled(newState);
    }
  };

  // Helper function to safely set ref for OpenVidu elements
  const createVideoRefCallback = (videoElement: Publisher | Subscriber) => {
    return (node: HTMLVideoElement | null) => {
      if (node && videoElement) {
        videoElement.addVideoElement(node);
      }
    };
  };

  return (
    <div className="room-container">
      {/* Main Video Area */}
      <div className="video-area">
        <div style={{ padding: '0 0 20px', display: 'flex', alignItems: 'center', gap: '16px' }}>
          <button className="btn-icon btn-secondary" onClick={() => { leaveVideoSession(); navigate('/'); }}>
            <ArrowLeft size={20} />
          </button>
          <h2>Workspace: {roomId?.slice(0, 8)}...</h2>
        </div>

        <div className="video-grid">
          {videoJoined ? (
            <>
              {publisher && (
                <div className="video-box">
                  <video autoPlay={true} muted playsInline ref={createVideoRefCallback(publisher)} style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                  <div style={{ position: 'absolute', bottom: '10px', left: '10px', background: 'rgba(0,0,0,0.6)', padding: '4px 8px', borderRadius: '4px', fontSize: '12px' }}>
                    {user?.username} (You)
                  </div>
                </div>
              )}
              {subscribers.map((sub, i) => {
                let clientData = 'Remote User';
                try {
                  const data = sub.stream.connection.data;
                  if (data) {
                    clientData = JSON.parse(data.split('%/%')[0]).clientData;
                  }
                } catch(e) {}
                
                return (
                  <div className="video-box" key={i}>
                    <video autoPlay={true} playsInline ref={createVideoRefCallback(sub)} style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                    <div style={{ position: 'absolute', bottom: '10px', left: '10px', background: 'rgba(0,0,0,0.6)', padding: '4px 8px', borderRadius: '4px', fontSize: '12px' }}>
                      {clientData}
                    </div>
                  </div>
                );
              })}
            </>
          ) : (
            <div className="video-box" style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', flexDirection: 'column', gap: '16px' }}>
              {videoError ? (
                <>
                  <p style={{ color: '#ef4444' }}>{videoError}</p>
                  <button className="btn-primary" onClick={joinVideoSession}>Retry</button>
                </>
              ) : (
                <>
                  <Video size={48} color="var(--text-muted)" />
                  <p style={{ color: 'var(--text-muted)' }}>Camera preview</p>
                  <button className="btn-primary" onClick={joinVideoSession}>
                    Join Video Call
                  </button>
                </>
              )}
            </div>
          )}
        </div>

        <div className="video-controls glass">
          <button className={audioEnabled ? 'btn-secondary' : 'btn-danger'} onClick={toggleAudio} disabled={!videoJoined}>
            {audioEnabled ? <Mic size={20} /> : <MicOff size={20} />}
          </button>
          <button className={videoEnabled ? 'btn-secondary' : 'btn-danger'} onClick={toggleVideo} disabled={!videoJoined}>
            {videoEnabled ? <Video size={20} /> : <VideoOff size={20} />}
          </button>
          <button className="btn-danger" onClick={() => { leaveVideoSession(); navigate('/'); }}>
            Leave Session
          </button>
        </div>
      </div>

      {/* Chat Sidebar */}
      <div className="chat-sidebar">
        <div className="chat-header">Team Chat</div>
        <div className="chat-messages">
          {messages.map((msg, i) => {
            const isOwn = msg.senderUsername === user?.username;
            let timeString = '';
            try {
              const dateVal = msg.sentAt ? new Date(msg.sentAt) : new Date();
              timeString = dateVal.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
            } catch(e) {
              timeString = 'Now';
            }
            return (
              <div key={i} className={`message animate-fade-in ${isOwn ? 'own' : ''}`}>
                <div 
                  className="message-sender" 
                  style={{ display: 'flex', gap: '8px', justifyContent: isOwn ? 'flex-end' : 'flex-start', alignItems: 'center', marginBottom: '4px' }}
                >
                  <span style={{ fontWeight: 'bold', fontSize: '0.85rem' }}>{isOwn ? 'You' : msg.senderUsername}</span>
                  <span style={{ fontSize: '0.75rem', color: '#a1a1aa' }}>{timeString}</span>
                </div>
                <div className="message-bubble">{msg.content}</div>
              </div>
            );
          })}
          <div ref={messagesEndRef} />
        </div>
        <div className="chat-input" style={{ display: 'flex', gap: '8px', padding: '16px' }}>
          <input 
            type="text" 
            placeholder="Type a message..." 
            value={chatInput} 
            onChange={e => setChatInput(e.target.value)}
            onKeyDown={e => { if (e.key === 'Enter') sendMessage(); }}
          />
          <button type="button" className="btn-primary" style={{ padding: '12px' }} onClick={sendMessage}>
            <Send size={18} />
          </button>
        </div>
      </div>
    </div>
  );
}
