package com.example.collab.signalling

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * Spring Boot application entry point for the Signalling Service.
 *
 * Provides gRPC-based WebRTC signalling and room routing capabilities.
 */
@SpringBootApplication
class SignallingServiceApplication

/**
 * Main application launcher for Signalling Service.
 *
 * @param args Command line arguments
 */
fun main(args: Array<String>) {
    runApplication<SignallingServiceApplication>(*args)
}
