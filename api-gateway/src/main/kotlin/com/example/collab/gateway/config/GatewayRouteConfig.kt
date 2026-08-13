package com.example.collab.gateway.config

import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Programmatic route definitions.
 *
 * These supplement the YAML-based routes in application.yml.
 * Use this class when you need dynamic or conditional routing logic.
 */
@Configuration
class GatewayRouteConfig {

    @Bean
    fun customRoutes(builder: RouteLocatorBuilder): RouteLocator =
        builder.routes()
            // Health aggregation endpoint
            .route("health-check") { r ->
                r.path("/health")
                    .filters { f -> f.setPath("/actuator/health") }
                    .uri("http://localhost:8080")
            }
            .build()
}
