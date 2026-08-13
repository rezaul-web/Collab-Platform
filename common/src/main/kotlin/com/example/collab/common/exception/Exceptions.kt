package com.example.collab.common.exception

/**
 * Base sealed exception class for application domain errors with HTTP status codes.
 *
 * @param message Error message details
 * @property status HTTP status code associated with the exception
 */
sealed class CollabException(
    message: String,
    val status: Int
) : RuntimeException(message)

/**
 * Exception thrown when a requested resource is not found (HTTP 404).
 *
 * @param message Description of the missing resource
 */
class NotFoundException(message: String) : CollabException(message, 404)

/**
 * Exception thrown when authentication or authorization fails (HTTP 401).
 *
 * @param message Cause of unauthorized access
 */
class UnauthorizedException(message: String) : CollabException(message, 401)

/**
 * Exception thrown when client request is invalid or malformed (HTTP 400).
 *
 * @param message Validation error description
 */
class BadRequestException(message: String) : CollabException(message, 400)

/**
 * Exception thrown when a resource conflict occurs, e.g., duplicate entity (HTTP 409).
 *
 * @param message Conflict details
 */
class ConflictException(message: String) : CollabException(message, 409)
