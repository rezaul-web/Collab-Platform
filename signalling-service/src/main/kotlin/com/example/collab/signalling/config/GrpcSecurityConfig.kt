package com.example.collab.signalling.config

import net.devh.boot.grpc.server.security.authentication.BasicGrpcAuthenticationReader
import net.devh.boot.grpc.server.security.authentication.GrpcAuthenticationReader
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Configures gRPC server security integration with Spring Security.
 */
@Configuration
class GrpcSecurityConfig {

    @Bean
    fun grpcAuthenticationReader(): GrpcAuthenticationReader {
        return BasicGrpcAuthenticationReader()
    }
}
