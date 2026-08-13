import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { restApi } from '../api';
import { useAuthStore } from '../store';
import { UserPlus } from 'lucide-react';

export default function Register() {
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const setAuth = useAuthStore(state => state.setAuth);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    try {
      const res = await restApi.register({ username, email, password });
      setAuth(res.token, { id: '', username: res.username, role: 'USER' });
      navigate('/');
    } catch (err: any) {
      setError(err.message || 'Failed to register');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-container">
      <div className="glass auth-card animate-fade-in">
        <h2 className="text-gradient" style={{ textAlign: 'center', fontSize: '2rem' }}>Create Account</h2>
        <p style={{ textAlign: 'center', color: 'var(--text-muted)', marginTop: '8px' }}>Join the Collab Platform</p>
        
        <form onSubmit={handleSubmit}>
          {error && <div style={{ color: 'var(--danger)', fontSize: '14px', textAlign: 'center' }}>{error}</div>}
          
          <div>
            <input 
              type="text" 
              placeholder="Username" 
              value={username} 
              onChange={e => setUsername(e.target.value)} 
              required 
            />
          </div>
          <div>
            <input 
              type="email" 
              placeholder="Email Address" 
              value={email} 
              onChange={e => setEmail(e.target.value)} 
              required 
            />
          </div>
          <div>
            <input 
              type="password" 
              placeholder="Password" 
              value={password} 
              onChange={e => setPassword(e.target.value)} 
              required 
            />
          </div>
          
          <button type="submit" className="btn-primary" disabled={loading}>
            <UserPlus size={18} />
            {loading ? 'Creating...' : 'Create Account'}
          </button>
        </form>
        
        <div className="auth-link">
          Already have an account? <Link to="/login">Sign In</Link>
        </div>
      </div>
    </div>
  );
}
