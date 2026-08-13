rootProject.name = "collab-platform-kotlin"

// ── Shared libraries ─────────────────────────────────────────────────
include(":proto")                   // Protobuf / gRPC stub generation
include(":common")                  // Shared DTOs, security, exceptions

// ── Microservices ────────────────────────────────────────────────────
include(":auth-service")            // User registration, login, JWT
include(":chat-service")            // WebSocket STOMP, GraphQL, messaging
include(":signalling-service")      // gRPC WebRTC signalling
include(":media-service")           // OpenVidu WebRTC session management
include(":api-gateway")             // API Gateway — routes to all services
