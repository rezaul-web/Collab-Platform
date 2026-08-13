package com.example.collab.auth.config

import net.devh.boot.grpc.server.security.authentication.BasicGrpcAuthenticationReader
import net.devh.boot.grpc.server.security.authentication.GrpcAuthenticationReader
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Configures gRPC server security integration with Spring Security.
 *
 * Provides a [GrpcAuthenticationReader] bean required by the grpc-spring-boot-starter
 * when Spring Security is on the classpath.
 */
@Configuration
class GrpcSecurityConfig {

    @Bean
    fun grpcAuthenticationReader(): GrpcAuthenticationReader {
        return BasicGrpcAuthenticationReader()
    }
}
