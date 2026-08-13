import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    host: true,
    proxy: {
      '/api/auth': { target: 'http://auth-service:8081', changeOrigin: true },
      '/graphql': { 
        target: 'http://chat-service:8082', 
        changeOrigin: true,
        configure: (proxy, options) => {
          proxy.on('error', (err, req, res) => {
            console.log('proxy error', err);
          });
          proxy.on('proxyReq', (proxyReq, req, res) => {
            console.log('Sending Request to the Target:', req.method, req.url);
          });
          proxy.on('proxyRes', (proxyRes, req, res) => {
            console.log('Received Response from the Target:', proxyRes.statusCode, req.url);
          });
        }
      },
      '/ws/raw': { target: 'http://chat-service:8082', ws: true, changeOrigin: true },
      '/ws': { target: 'http://chat-service:8082', ws: true, changeOrigin: true },
      '/api/media': { target: 'http://media-service:8084', changeOrigin: true }
    }
  }
})
