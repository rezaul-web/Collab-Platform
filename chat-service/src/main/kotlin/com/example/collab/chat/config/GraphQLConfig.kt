package com.example.collab.chat.config

import graphql.scalars.ExtendedScalars
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.graphql.execution.RuntimeWiringConfigurer

/**
 * Configuration class for GraphQL runtime wiring and custom scalar registrations.
 */
@Configuration
class GraphQLConfig {

    /**
     * Registers custom scalars such as [ExtendedScalars.DateTime].
     *
     * @return Configured [RuntimeWiringConfigurer]
     */
    @Bean
    fun runtimeWiringConfigurer(): RuntimeWiringConfigurer {
        return RuntimeWiringConfigurer { builder ->
            builder.scalar(ExtendedScalars.DateTime)
        }
    }
}
