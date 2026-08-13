package com.example.collab.common.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/**
 * Spring configuration class for setting up Cross-Origin Resource Sharing (CORS) rules.
 */
@Configuration
class CorsConfig {

    /**
     * Creates a [WebMvcConfigurer] bean configuring CORS mappings allowing all origins,
     * HTTP methods, and headers across all endpoint patterns.
     *
     * @return Configured [WebMvcConfigurer]
     */
    @Bean
    fun corsConfigurer(): WebMvcConfigurer {
        return object : WebMvcConfigurer {
            override fun addCorsMappings(registry: CorsRegistry) {
                registry.addMapping("/**")
                    .allowedOrigins("*")
                    .allowedMethods("*")
                    .allowedHeaders("*")
            }
        }
    }
}
