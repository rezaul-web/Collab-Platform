package com.example.collab.gateway

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * API Gateway — single entry point for all microservices.
 *
 * Routes incoming HTTP requests to the appropriate downstream
 * microservice based on path prefixes configured in application.yml.
 *
 * Runs on port **8000** (the only public-facing port).
 *
 * Excludes component scanning of com.example.collab.common to avoid
 * pulling in servlet-based security beans into this reactive application.
 */
@SpringBootApplication(scanBasePackages = ["com.example.collab.gateway"])
class GatewayApplication

fun main(args: Array<String>) {
    runApplication<GatewayApplication>(*args)
}
