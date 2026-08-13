import { GraphQLClient } from 'graphql-request';

const API_BASE = 'http://localhost:8000';

export const getAuthHeaders = () => {
  const token = localStorage.getItem('token');
  return token ? { Authorization: `Bearer ${token}` } : {};
};

// --- GraphQL Client ---
export const gqlClient = new GraphQLClient(`${API_BASE}/graphql`, {
  requestMiddleware: (request) => {
    const token = localStorage.getItem('token');
    return {
      ...request,
      headers: { ...request.headers, ...(token ? { Authorization: `Bearer ${token}` } : {}) }
    };
  }
});

// --- REST APIs ---
export const restApi = {
  login: async (credentials: any) => {
    const res = await fetch(`${API_BASE}/api/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(credentials)
    });
    if (!res.ok) throw new Error('Login failed');
    return res.json();
  },
  
  register: async (credentials: any) => {
    const res = await fetch(`${API_BASE}/api/auth/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(credentials)
    });
    if (!res.ok) throw new Error('Registration failed');
    return res.json();
  },

  createMediaSession: async (roomId: string) => {
    const res = await fetch(`${API_BASE}/api/media/sessions`, {
      method: 'POST',
      headers: { ...getAuthHeaders(), 'Content-Type': 'application/json' },
      body: JSON.stringify({ roomId })
    });
    if (!res.ok) throw new Error('Failed to create media session');
    return res.json();
  },

  generateMediaToken: async (sessionId: string) => {
    const res = await fetch(`${API_BASE}/api/media/sessions/${sessionId}/connections`, {
      method: 'POST',
      headers: { ...getAuthHeaders(), 'Content-Type': 'application/json' }
    });
    if (!res.ok) throw new Error('Failed to generate media token');
    return res.json();
  }
};
