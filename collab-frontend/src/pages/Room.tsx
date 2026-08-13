import { useState, useEffect, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { OpenVidu, Publisher, Subscriber, Session as OVSession } from 'openvidu-browser';
import { Client } from '@stomp/stompjs';
import { restApi, gqlClient } from '../api';
import { useAuthStore } from '../store';
import { gql } from 'graphql-request';
import { Send, ArrowLeft, Mic, MicOff, Video, VideoOff } from 'lucide-react';

const GET_MESSAGES = gql`
  query GetMessages($roomId: ID!) {
    messages(roomId: $roomId, page: 0, size: 50) {
      messages {
        id
        content
        sentAt
        senderUsername
      }
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
  const [session, setSession] = useState<OVSession | null>(null);
  const [publisher, setPublisher] = useState<Publisher | null>(null);
  const [subscribers, setSubscribers] = useState<Subscriber[]>([]);
  const [audioEnabled, setAudioEnabled] = useState(true);
  const [videoEnabled, setVideoEnabled] = useState(true);

  useEffect(() => {
    if (!roomId || !user) return;
    
    fetchPreviousMessages();
    connectChat();
    joinVideoSession();

    return () => {
      leaveVideoSession();
      if (stompClient.current) {
        stompClient.current.deactivate();
      }
    };
  }, [roomId, user]);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  // --- CHAT LOGIC ---
  const fetchPreviousMessages = async () => {
    try {
      const data: any = await gqlClient.request(GET_MESSAGES, { roomId });
      setMessages(data.messages.messages.reverse()); // Chronological order
    } catch (err) {
      console.error('Failed to fetch messages', err);
    }
  };

  const connectChat = () => {
    const client = new Client({
      brokerURL: 'ws://localhost:8000/ws/chat',
      connectHeaders: { Authorization: `Bearer ${token}` },
      debug: function (str) { console.log(str); },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });

    client.onConnect = () => {
      console.log('STOMP Connected');
      client.subscribe(`/topic/room.${roomId}`, (msg) => {
        const newMsg = JSON.parse(msg.body);
        setMessages(prev => [...prev, {
          id: Date.now().toString(),
          content: newMsg.content,
          senderUsername: newMsg.from,
          sentAt: newMsg.timestamp || new Date().toISOString()
        }]);
      });
    };

    client.activate();
    stompClient.current = client;
  };

  const sendMessage = (e: React.FormEvent) => {
    e.preventDefault();
    if (!chatInput.trim() || !stompClient.current?.connected) return;
    
    stompClient.current.publish({
      destination: '/app/chat.send',
      body: JSON.stringify({ roomId, content: chatInput })
    });
    setChatInput('');
  };

  // --- VIDEO LOGIC ---
  const joinVideoSession = async () => {
    try {
      const OV = new OpenVidu();
      const mySession = OV.initSession();

      mySession.on('streamCreated', (event) => {
        const subscriber = mySession.subscribe(event.stream, undefined);
        setSubscribers(prev => [...prev, subscriber]);
      });

      mySession.on('streamDestroyed', (event) => {
        setSubscribers(prev => prev.filter(sub => sub !== event.stream.streamManager));
      });

      // Get Token from backend
      await restApi.createMediaSession(roomId!);
      const { token: ovToken } = await restApi.generateMediaToken(roomId!);

      await mySession.connect(ovToken, { clientData: user?.username });

      const pub = await OV.initPublisherAsync(undefined, {
        audioSource: undefined,
        videoSource: undefined,
        publishAudio: true,
        publishVideo: true,
        resolution: '640x480',
        frameRate: 30,
        insertMode: 'APPEND',
        mirror: false
      });

      mySession.publish(pub);
      setSession(mySession);
      setPublisher(pub);

    } catch (err) {
      console.error('Error joining video session', err);
    }
  };

  const leaveVideoSession = () => {
    if (session) session.disconnect();
    setSession(null);
    setPublisher(null);
    setSubscribers([]);
  };

  const toggleAudio = () => {
    if (publisher) {
      publisher.publishAudio(!audioEnabled);
      setAudioEnabled(!audioEnabled);
    }
  };

  const toggleVideo = () => {
    if (publisher) {
      publisher.publishVideo(!videoEnabled);
      setVideoEnabled(!videoEnabled);
    }
  };

  return (
    <div className="room-container">
      {/* Main Video Area */}
      <div className="video-area">
        <div style={{ padding: '0 0 20px', display: 'flex', alignItems: 'center', gap: '16px' }}>
          <button className="btn-icon btn-secondary" onClick={() => navigate('/')}>
            <ArrowLeft size={20} />
          </button>
          <h2>Workspace: {roomId}</h2>
        </div>

        <div className="video-grid">
          {publisher && (
            <div className="video-box">
              <video autoPlay={true} ref={node => node && publisher.addVideoElement(node)} />
              <div style={{ position: 'absolute', bottom: '10px', left: '10px', background: 'rgba(0,0,0,0.6)', padding: '4px 8px', borderRadius: '4px', fontSize: '12px' }}>
                {user?.username} (You)
              </div>
            </div>
          )}
          {subscribers.map((sub, i) => {
            const clientData = sub.stream.connection.data ? JSON.parse(sub.stream.connection.data).clientData : 'Remote User';
            return (
              <div className="video-box" key={i}>
                <video autoPlay={true} ref={node => node && sub.addVideoElement(node)} />
                <div style={{ position: 'absolute', bottom: '10px', left: '10px', background: 'rgba(0,0,0,0.6)', padding: '4px 8px', borderRadius: '4px', fontSize: '12px' }}>
                  {clientData}
                </div>
              </div>
            );
          })}
        </div>

        <div className="video-controls glass">
          <button className={audioEnabled ? 'btn-secondary' : 'btn-danger'} onClick={toggleAudio}>
            {audioEnabled ? <Mic size={20} /> : <MicOff size={20} />}
          </button>
          <button className={videoEnabled ? 'btn-secondary' : 'btn-danger'} onClick={toggleVideo}>
            {videoEnabled ? <Video size={20} /> : <VideoOff size={20} />}
          </button>
          <button className="btn-danger" onClick={() => navigate('/')}>
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
        <form className="chat-input" onSubmit={sendMessage}>
          <input 
            type="text" 
            placeholder="Type a message..." 
            value={chatInput} 
            onChange={e => setChatInput(e.target.value)} 
          />
          <button type="submit" className="btn-primary" style={{ padding: '12px' }}>
            <Send size={18} />
          </button>
        </form>
      </div>
    </div>
  );
}
