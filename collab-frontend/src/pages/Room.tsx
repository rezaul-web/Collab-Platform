import { useState, useEffect, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { restApi } from '../api';
import { useAuthStore } from '../store';
import { Client } from '@stomp/stompjs';
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
  const localVideoRef = useRef<HTMLVideoElement>(null);
  const [localStream, setLocalStream] = useState<MediaStream | null>(null);
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
      if (localStream) {
        localStream.getTracks().forEach(t => t.stop());
      }
    };
  }, [roomId, user]);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  // --- CHAT LOGIC ---
  const fetchPreviousMessages = async () => {
    try {
      const data = await gqlFetch(GET_MESSAGES_QUERY, { roomId });
      // Schema returns [Message!]! directly, not wrapped
      const msgs = Array.isArray(data.messages) ? data.messages : [];
      setMessages(msgs.slice().reverse()); // Chronological order
    } catch (err) {
      console.error('Failed to fetch messages', err);
    }
  };

  const connectChat = () => {
    const wsProtocol = window.location.protocol === 'https:' ? 'wss' : 'ws';
    const client = new Client({
      brokerURL: `${wsProtocol}://${window.location.host}/ws`,
      connectHeaders: { Authorization: `Bearer ${token}` },
      debug: function (str) { console.log('[STOMP]', str); },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });

    client.onConnect = () => {
      console.log('STOMP Connected!');
      // Subscribe to the topic matching the backend @SendTo annotation
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
    
    // Send to the destination matching backend @MessageMapping("/chat/{roomId}")
    stompClient.current.publish({
      destination: `/app/chat/${roomId}`,
      body: JSON.stringify({ 
        from: user?.username, 
        content: chatInput 
      })
    });
    setChatInput('');
  };

  // --- VIDEO LOGIC (simplified without OpenVidu dependency) ---
  const joinVideoSession = async () => {
    try {
      setVideoError(null);
      
      // Get local media
      const stream = await navigator.mediaDevices.getUserMedia({
        video: true,
        audio: true
      });
      setLocalStream(stream);
      
      if (localVideoRef.current) {
        localVideoRef.current.srcObject = stream;
      }

      // Try to create media session on backend
      try {
        const sessionData = await restApi.createMediaSession(roomId!);
        console.log('Media session created:', sessionData);
        const tokenData = await restApi.generateMediaToken(sessionData.sessionId);
        console.log('Media token:', tokenData);
      } catch (mediaErr) {
        console.warn('Media backend not fully available, using local preview only:', mediaErr);
      }
      
      setVideoJoined(true);
    } catch (err: any) {
      console.error('Error joining video session:', err);
      setVideoError(err.message || 'Failed to access camera/microphone');
    }
  };

  const leaveVideoSession = () => {
    if (localStream) {
      localStream.getTracks().forEach(t => t.stop());
      setLocalStream(null);
    }
    if (localVideoRef.current) {
      localVideoRef.current.srcObject = null;
    }
    setVideoJoined(false);
  };

  const toggleAudio = () => {
    if (localStream) {
      localStream.getAudioTracks().forEach(t => { t.enabled = !audioEnabled; });
      setAudioEnabled(!audioEnabled);
    }
  };

  const toggleVideo = () => {
    if (localStream) {
      localStream.getVideoTracks().forEach(t => { t.enabled = !videoEnabled; });
      setVideoEnabled(!videoEnabled);
    }
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
            <div className="video-box">
              <video autoPlay muted playsInline ref={localVideoRef} style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
              <div style={{ position: 'absolute', bottom: '10px', left: '10px', background: 'rgba(0,0,0,0.6)', padding: '4px 8px', borderRadius: '4px', fontSize: '12px' }}>
                {user?.username} (You)
              </div>
            </div>
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
            return (
              <div key={i} className={`message animate-fade-in ${isOwn ? 'own' : ''}`}>
                {!isOwn && <div className="message-sender">{msg.senderUsername}</div>}
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
