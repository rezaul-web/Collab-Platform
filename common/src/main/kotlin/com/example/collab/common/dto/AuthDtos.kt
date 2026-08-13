package com.example.collab.common.dto

/**
 * Data transfer object for user registration request.
 *
 * @property username User's chosen username
 * @property email User's email address
 * @property password User's plain text password
 */
data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String
)

/**
 * Data transfer object for user authentication/login request.
 *
 * @property username User's username
 * @property password User's plain text password
 */
data class LoginRequest(
    val username: String,
    val password: String
)

/**
 * Data transfer object for authentication response containing JWT token.
 *
 * @property token The generated JWT bearer token
 * @property username Authenticated user's username
 * @property expiresIn Token validity duration in milliseconds
 */
data class AuthResponse(
    val token: String,
    val username: String,
    val expiresIn: Long
)

/**
 * Data transfer object representing user profile information.
 *
 * @property id Unique user identifier
 * @property username User's username
 * @property email User's email address
 * @property displayName User's display name, if configured
 * @property role User's assigned role
 */
data class UserDto(
    val id: String,
    val username: String,
    val email: String,
    val displayName: String?,
    val role: String
)
