package com.example.collab.signalling.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Health check REST controller for the Signalling Service.
 */
@RestController
@RequestMapping("/api/signalling")
class SignallingHealthController {

    /**
     * Endpoint to check health status of the service.
     *
     * @return Status map indicating UP status and service identifier
     */
    @GetMapping("/health")
    fun health(): Map<String, String> {
        return mapOf(
            "status" to "UP",
            "service" to "signalling-service"
        )
    }
}
