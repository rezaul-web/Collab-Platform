package com.example.collab.gateway.filter

import org.slf4j.LoggerFactory
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.core.Ordered
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

/**
 * Global gateway filter that validates JWT tokens on protected routes.
 *
 * Public paths (auth endpoints, health, GraphiQL) are whitelisted.
 * All other requests must carry a valid `Authorization: Bearer <token>` header.
 *
 * NOTE: Full token validation is delegated to downstream services.
 * This filter only checks for the presence of the header as a first gate.
 */
@Component
class JwtGatewayFilter : GlobalFilter, Ordered {

    private val log = LoggerFactory.getLogger(JwtGatewayFilter::class.java)

    private val publicPaths = listOf(
        "/api/auth/register",
        "/api/auth/login",
        "/api/signalling/health",
        "/api/media/health",
        "/actuator",
        "/graphiql",
        "/health"
    )

    override fun filter(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> {
        val path = exchange.request.uri.path

        // Skip auth check for public endpoints
        if (publicPaths.any { path.startsWith(it) }) {
            return chain.filter(exchange)
        }

        val authHeader = exchange.request.headers.getFirst("Authorization")
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Missing or invalid Authorization header for path: {}", path)
            exchange.response.statusCode = HttpStatus.UNAUTHORIZED
            return exchange.response.setComplete()
        }

        // Forward the token downstream — the individual service validates it
        return chain.filter(exchange)
    }

    override fun getOrder(): Int = -1 // Run early
}
