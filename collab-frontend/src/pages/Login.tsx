import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { restApi } from '../api';
import { useAuthStore } from '../store';
import { LogIn } from 'lucide-react';

export default function Login() {
  const [username, setUsername] = useState('');
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
      const res = await restApi.login({ username, password });
      setAuth(res.token, { id: '', username: res.username, role: 'USER' }); // Decode JWT later if needed
      navigate('/');
    } catch (err: any) {
      setError(err.message || 'Failed to login');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-container">
      <div className="glass auth-card animate-fade-in">
        <h2 className="text-gradient" style={{ textAlign: 'center', fontSize: '2rem' }}>Welcome Back</h2>
        <p style={{ textAlign: 'center', color: 'var(--text-muted)', marginTop: '8px' }}>Sign in to continue to Collab Platform</p>
        
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
              type="password" 
              placeholder="Password" 
              value={password} 
              onChange={e => setPassword(e.target.value)} 
              required 
            />
          </div>
          
          <button type="submit" className="btn-primary" disabled={loading}>
            <LogIn size={18} />
            {loading ? 'Signing in...' : 'Sign In'}
          </button>
        </form>
        
        <div className="auth-link">
          Don't have an account? <Link to="/register">Create one</Link>
        </div>
      </div>
    </div>
  );
}
