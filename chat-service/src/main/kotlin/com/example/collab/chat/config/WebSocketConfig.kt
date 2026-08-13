package com.example.collab.chat.config

import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer

/**
 * WebSocket STOMP broker configuration for real-time messaging.
 */
@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig : WebSocketMessageBrokerConfigurer {

    /**
     * Registers STOMP endpoints:
     * - `/ws/raw` for native WebSocket clients (browsers)
     * - `/ws` with SockJS fallback for legacy clients
     *
     * @param registry Endpoint registry
     */
    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        // Raw WebSocket endpoint (for @stomp/stompjs in browser)
        registry.addEndpoint("/ws/raw")
            .setAllowedOriginPatterns("*")

        // SockJS fallback endpoint
        registry.addEndpoint("/ws")
            .setAllowedOriginPatterns("*")
            .withSockJS()
    }

    /**
     * Configures message broker prefixes for application destinations and message routing.
     *
     * @param registry Message broker registry
     */
    override fun configureMessageBroker(registry: MessageBrokerRegistry) {
        registry.enableSimpleBroker("/topic", "/queue")
        registry.setApplicationDestinationPrefixes("/app")
    }
}
