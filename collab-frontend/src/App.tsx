import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { useAuthStore } from './store';
import Login from './pages/Login';
import Register from './pages/Register';
import Dashboard from './pages/Dashboard';
import Room from './pages/Room';

function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const token = useAuthStore(state => state.token);
  if (!token) return <Navigate to="/login" />;
  return <>{children}</>;
}

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        
        <Route path="/" element={
          <ProtectedRoute>
            <Dashboard />
          </ProtectedRoute>
        } />
        
        <Route path="/room/:id" element={
          <ProtectedRoute>
            <Room />
          </ProtectedRoute>
        } />
      </Routes>
    </BrowserRouter>
  );
}
