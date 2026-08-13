package com.example.collab.media.controller

import com.example.collab.media.service.OpenViduService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * REST controller for media session management and connection token generation.
 *
 * @property openViduService Service interacting with OpenVidu media server
 */
@RestController
@RequestMapping("/api/media")
class MediaController(
    private val openViduService: OpenViduService
) {

    /**
     * Creates a new media session.
     *
     * @param request Map optionally containing "roomId"
     * @return Response entity containing created sessionId
     */
    @PostMapping("/sessions")
    fun createSession(@RequestBody request: Map<String, String?>): ResponseEntity<Map<String, String>> {
        val roomId = request["roomId"]
        val sessionId = openViduService.createSession(roomId)
        return ResponseEntity.ok(mapOf("sessionId" to sessionId))
    }

    /**
     * Creates a connection token for an existing media session.
     *
     * @param sessionId Target session identifier
     * @return Response entity containing generated token
     */
    @PostMapping("/sessions/{sessionId}/connections")
    fun createConnection(@PathVariable sessionId: String): ResponseEntity<Map<String, String>> {
        val token = openViduService.generateToken(sessionId)
        return ResponseEntity.ok(mapOf("token" to token))
    }

    /**
     * Endpoint to check health status of the media service.
     *
     * @return Response entity containing health status and service identifier
     */
    @GetMapping("/health")
    fun health(): ResponseEntity<Map<String, String>> {
        return ResponseEntity.ok(
            mapOf(
                "status" to "UP",
                "service" to "media-service"
            )
        )
    }
}
