package com.example.collab.media

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

/**
 * Spring Boot application entry point for the Media Service.
 *
 * Manages WebRTC media session creation and connection tokens via OpenVidu.
 */
@SpringBootApplication
class MediaServiceApplication

/**
 * Main application launcher for Media Service.
 *
 * @param args Command line arguments
 */
fun main(args: Array<String>) {
    runApplication<MediaServiceApplication>(*args)
}
