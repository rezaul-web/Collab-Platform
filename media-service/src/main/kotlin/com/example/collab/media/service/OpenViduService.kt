package com.example.collab.media.service

import com.example.collab.common.exception.NotFoundException
import io.openvidu.java.client.ConnectionProperties
import io.openvidu.java.client.OpenVidu
import io.openvidu.java.client.OpenViduHttpException
import io.openvidu.java.client.SessionProperties
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

/**
 * Service providing OpenVidu session creation and connection token generation.
 *
 * @property openViduUrl URL of the OpenVidu server endpoint
 * @property openViduSecret Secret key for OpenVidu server authentication
 */
@Service
class OpenViduService(
    @Value("\${app.openvidu.url}") private val openViduUrl: String,
    @Value("\${app.openvidu.secret}") private val openViduSecret: String
) {
    private val log = LoggerFactory.getLogger(OpenViduService::class.java)

    private val openVidu by lazy { OpenVidu(openViduUrl, openViduSecret) }

    /**
     * Creates a new WebRTC session on the OpenVidu server.
     *
     * @param roomId Optional room ID to use as custom session ID
     * @return The unique session ID created
     */
    fun createSession(roomId: String?): String {
        log.info("Creating OpenVidu session for room ID: {}", roomId)
        val builder = SessionProperties.Builder()
        if (!roomId.isNullOrBlank()) {
            builder.customSessionId(roomId)
        }
        val properties = builder.build()

        return try {
            val session = openVidu.createSession(properties)
            log.info("Created OpenVidu session with ID: {}", session.sessionId)
            session.sessionId
        } catch (e: OpenViduHttpException) {
            if (e.status == 409 && !roomId.isNullOrBlank()) {
                log.info("Session already exists for room ID '{}', using existing session ID", roomId)
                roomId
            } else {
                log.error("HTTP error creating OpenVidu session: status={}, message={}", e.status, e.message)
                throw e
            }
        } catch (e: Exception) {
            log.error("Unexpected error creating OpenVidu session: {}", e.message, e)
            throw e
        }
    }

    /**
     * Generates a connection token for an active OpenVidu session.
     *
     * @param sessionId Unique identifier of the target OpenVidu session
     * @return Connection token string for WebRTC peer client join
     * @throws NotFoundException if no active session with the given ID exists
     */
    fun generateToken(sessionId: String): String {
        log.info("Generating OpenVidu token for session: {}", sessionId)
        return try {
            openVidu.fetch()
            val session = openVidu.activeSessions.firstOrNull { it.sessionId == sessionId }
                ?: throw NotFoundException("Session not found with ID: $sessionId")

            val connectionProperties = ConnectionProperties.Builder().build()
            val connection = session.createConnection(connectionProperties)
            log.info("Generated token for session '{}'", sessionId)
            connection.token
        } catch (e: NotFoundException) {
            log.warn("Session not found during token generation: {}", e.message)
            throw e
        } catch (e: Exception) {
            log.error("Failed to generate token for session '{}': {}", sessionId, e.message, e)
            throw e
        }
    }
}
