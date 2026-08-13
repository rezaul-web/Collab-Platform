package com.example.collab.common.exception

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

/**
 * Global exception handler providing central error handling across controllers.
 */
@RestControllerAdvice
class GlobalExceptionHandler {
    private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    /**
     * Handles custom application exceptions defined in [CollabException].
     *
     * @param ex The caught [CollabException]
     * @return [ResponseEntity] with the status code and error details map
     */
    @ExceptionHandler(CollabException::class)
    fun handleCollabException(ex: CollabException): ResponseEntity<Map<String, String?>> {
        log.warn("Application exception occurred: status={}, message={}", ex.status, ex.message)
        return ResponseEntity.status(ex.status).body(mapOf("error" to ex.message))
    }

    /**
     * Fallback handler for unhandled generic exceptions.
     *
     * @param ex The caught [Exception]
     * @return [ResponseEntity] with HTTP 500 status and generic error message
     */
    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ResponseEntity<Map<String, String>> {
        log.error("Unhandled exception occurred: ", ex)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(mapOf("error" to "Internal server error"))
    }
}
