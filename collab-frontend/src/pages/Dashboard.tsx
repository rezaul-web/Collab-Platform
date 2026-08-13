import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '../store';
import { LogOut, Plus, Users, Video } from 'lucide-react';

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

const GET_ROOMS_QUERY = `
  query {
    rooms {
      id
      name
      description
      createdAt
      active
      participantCount
    }
  }
`;

const CREATE_ROOM_MUTATION = `
  mutation CreateRoom($name: String!, $description: String) {
    createRoom(name: $name, description: $description) {
      id
      name
    }
  }
`;

export default function Dashboard() {
  const [rooms, setRooms] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [newRoomName, setNewRoomName] = useState('');
  const [creating, setCreating] = useState(false);
  
  const user = useAuthStore(state => state.user);
  const logout = useAuthStore(state => state.logout);
  const navigate = useNavigate();

  useEffect(() => {
    fetchRooms();
  }, []);

  const fetchRooms = async () => {
    try {
      const data = await gqlFetch(GET_ROOMS_QUERY);
      setRooms(data.rooms);
    } catch (err) {
      console.error("Failed to fetch rooms", err);
    } finally {
      setLoading(false);
    }
  };

  const handleCreateRoom = async () => {
    console.log("=== CREATE ROOM CLICKED ===");
    console.log("Room name:", newRoomName);
    if (!newRoomName.trim()) {
      alert("Please enter a room name");
      return;
    }
    setCreating(true);
    try {
      console.log("Sending GraphQL mutation...");
      const data = await gqlFetch(CREATE_ROOM_MUTATION, { 
        name: newRoomName.trim(), 
        description: 'Created via Web' 
      });
      console.log("Room created:", data);
      alert("Room created successfully: " + data.createRoom.name);
      setNewRoomName('');
      fetchRooms();
    } catch (err: any) {
      console.error("Failed to create room:", err);
      alert("Failed to create room: " + err.message);
    } finally {
      setCreating(false);
    }
  };

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div className="app-container" style={{ display: 'block', overflow: 'auto' }}>
      <nav className="navbar glass">
        <h1 className="text-gradient" style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
          <Video size={28} color="#8b5cf6" />
          Collab Platform
        </h1>
        <div style={{ display: 'flex', alignItems: 'center', gap: '20px' }}>
          <span style={{ color: 'var(--text-muted)' }}>Hello, {user?.username}</span>
          <button onClick={handleLogout} className="btn-secondary" style={{ padding: '8px 16px' }}>
            <LogOut size={16} /> Logout
          </button>
        </div>
      </nav>

      <main style={{ maxWidth: '1200px', margin: '40px auto', padding: '0 20px' }}>
        <div className="glass-panel" style={{ padding: '30px', marginBottom: '40px' }}>
          <h2 style={{ marginBottom: '20px' }}>Create New Workspace</h2>
          <div style={{ display: 'flex', gap: '16px' }}>
            <input 
              type="text" 
              placeholder="Workspace Name (e.g. Design Sync)" 
              value={newRoomName}
              onChange={e => setNewRoomName(e.target.value)}
              style={{ maxWidth: '400px' }}
            />
            <button 
              type="button"
              className="btn-primary" 
              disabled={!newRoomName.trim() || creating}
              onClick={handleCreateRoom}
            >
              <Plus size={18} /> {creating ? 'Creating...' : 'Create Room'}
            </button>
          </div>
        </div>

        <h2 style={{ marginBottom: '20px' }}>Active Workspaces</h2>
        
        {loading ? (
          <div>Loading rooms...</div>
        ) : (
          <div className="room-grid" style={{ padding: 0 }}>
            {rooms.length === 0 ? (
              <p style={{ color: 'var(--text-muted)' }}>No rooms available. Create one above!</p>
            ) : (
              rooms.map(room => (
                <div 
                  key={room.id} 
                  className="glass room-card" 
                  onClick={() => navigate(`/room/${room.id}`)}
                >
                  <div className="room-header">
                    <div className="room-title">{room.name}</div>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '6px', color: 'var(--text-muted)', fontSize: '12px' }}>
                      <Users size={14} /> {room.participantCount}
                    </div>
                  </div>
                  <div className="room-desc">{room.description || 'No description provided.'}</div>
                  <div style={{ fontSize: '12px', color: 'var(--primary)' }}>Click to join workspace &rarr;</div>
                </div>
              ))
            )}
          </div>
        )}
      </main>
    </div>
  );
}
