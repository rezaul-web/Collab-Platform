package com.example.collab.auth

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

/**
 * Main entry point for the Auth Microservice.
 *
 * Scans both local auth components and shared common components for Spring beans.
 */
@SpringBootApplication
@ComponentScan(basePackages = ["com.example.collab.auth", "com.example.collab.common"])
class AuthServiceApplication

fun main(args: Array<String>) {
    runApplication<AuthServiceApplication>(*args)
}
