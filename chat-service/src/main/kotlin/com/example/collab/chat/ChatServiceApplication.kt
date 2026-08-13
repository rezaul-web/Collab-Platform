package com.example.collab.chat

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

/**
 * Main application entry point for the Chat Microservice.
 *
 * Provides real-time chat functionality, GraphQL APIs, and WebSocket STOMP messaging capabilities.
 */
@SpringBootApplication
@ComponentScan(basePackages = ["com.example.collab.chat", "com.example.collab.common"])
class ChatServiceApplication

/**
 * Main method to start the Spring Boot Chat Microservice application.
 *
 * @param args Command-line arguments passed to application
 */
fun main(args: Array<String>) {
    runApplication<ChatServiceApplication>(*args)
}
