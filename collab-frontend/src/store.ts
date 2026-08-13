import { create } from 'zustand';

interface User {
  id: string;
  username: string;
  role: string;
}

interface AuthState {
  token: string | null;
  user: User | null;
  setAuth: (token: string, user: User) => void;
  logout: () => void;
}

// Simple persist logic
const storedToken = localStorage.getItem('token');
const storedUser = localStorage.getItem('user');

export const useAuthStore = create<AuthState>((set) => ({
  token: storedToken,
  user: storedUser ? JSON.parse(storedUser) : null,
  setAuth: (token, user) => {
    localStorage.setItem('token', token);
    localStorage.setItem('user', JSON.stringify(user));
    set({ token, user });
  },
  logout: () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    set({ token: null, user: null });
  }
}));
