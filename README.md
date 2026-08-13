# Collab Platform

A real-time collaborative platform built with Kotlin, Spring Boot, and WebRTC. The system is designed as a scalable microservices architecture.

## Microservices Architecture

```text
                         ┌─────────────────┐
                         │     Client      │
                         │ Web / Android   │
                         └────────┬────────┘
                                  │
                         HTTP / WebSocket
                                  │
                                  ▼
                       ┌───────────────────┐
                       │    API Gateway    │
                       │      :8000        │
                       └─────────┬─────────┘
                                 │
          ┌──────────────────────┼──────────────────────┐
          │                      │                      │
          ▼                      ▼                      ▼
 ┌────────────────┐     ┌────────────────┐     ┌────────────────┐
 │ Auth Service   │     │ Chat Service   │     │ Media Service  │
 │    :8081       │     │    :8082       │     │    :8084       │
 │                │     │                │     │                │
 │ JWT            │     │ GraphQL        │     │ OpenVidu       │
 │ Login          │     │ WebSocket      │     │ Media sessions │
 │ Register       │     │ Rooms          │     │ Recording      │
 └───────┬────────┘     └───────┬────────┘     └───────┬────────┘
         │                      │                      │
         └──────────────┬───────┴──────────────┬───────┘
                        │                      │
                       gRPC                  gRPC
                        │                      │
                        ▼                      ▼
                  ┌───────────┐         ┌────────────┐
                  │ PostgreSQL│         │   Redis    │
                  └───────────┘         └────────────┘

                         WebRTC
                            │
                            ▼
                     ┌────────────┐
                     │  Signalling│
                     │   :8083    │
                     └─────┬──────┘
                           │
                           ▼
                       OpenVidu
                         :4443
```

The platform consists of the following components:

1. **API Gateway (Port 8000)**: A Spring Cloud Gateway that routes HTTP and WebSocket traffic to the appropriate backend microservices.
2. **Auth Service (Port 8081)**: Manages user registration, login, and JWT token generation. Connects to PostgreSQL.
3. **Chat Service (Port 8082)**: Provides a GraphQL API and STOMP over WebSockets for real-time messaging and room management. Connects to PostgreSQL and Auth Service via gRPC.
4. **Signalling Service (Port 8083)**: Handles WebRTC signalling (SDP offers, answers, and ICE candidates) to establish peer-to-peer connections. Connects to Auth & Chat services via gRPC.
5. **Media Service (Port 8084)**: Interfaces with OpenVidu to manage media sessions, recording, and broadcasting. Connects to Auth Service via gRPC.
6. **OpenVidu**: WebRTC media server running on port 4443.
7. **PostgreSQL**: Shared database for Auth, Chat, and Media services (each using a separate logical database).
8. **Redis**: In-memory data store for caching and pub/sub.

## Running the Application

The entire platform is containerized using Docker and Docker Compose.

### Prerequisites
- Docker and Docker Compose
- Java 21 (if building locally outside of Docker)

### Startup

1. Open a terminal in the root directory.
2. Run the following command to build the microservices and start all containers:
   ```bash
   docker-compose up -d --build
   ```
3. The API Gateway will be available at `http://localhost:8000`.

### Teardown

To stop the containers and preserve data:
```bash
docker-compose down
```

To stop the containers and wipe the database volumes:
```bash
docker-compose down -v
```

## API Documentation

All external API requests must go through the API Gateway on `http://localhost:8000`.

### Authentication (`/api/auth`)
- `POST /api/auth/register`: Register a new user (`{ username, email, password }`).
- `POST /api/auth/login`: Authenticate and receive a JWT token (`{ username, password }`).
- `GET /api/auth/me`: Get current user details (Requires `Authorization: Bearer <token>`).

### Chat & Rooms (GraphQL at `/graphql`)
- Requires `Authorization: Bearer <token>`.
- Example Queries: `rooms`, `messages(roomId: String)`.
- Example Mutations: `createRoom`, `sendMessage`.

### WebSocket (STOMP at `/ws/chat`)
- Clients connect to `ws://localhost:8000/ws/chat`.
- Subscribe to `/topic/room.{roomId}` to receive real-time messages.

## Inter-Service Communication

Services communicate securely over the internal Docker network:
1. **HTTP/REST**: The Gateway routes external HTTP requests to internal microservices.
2. **gRPC**: Backend microservices communicate synchronously with each other (e.g. Chat Service verifying tokens with Auth Service) using gRPC on ports `9091`, `9092`, `9093`.
3. **WebSockets**: Real-time traffic flows through the Gateway to the Chat Service.
