package com.example.collab.auth.service

import com.example.collab.auth.model.User
import com.example.collab.auth.repository.UserRepository
import com.example.collab.common.dto.AuthResponse
import com.example.collab.common.dto.LoginRequest
import com.example.collab.common.dto.RegisterRequest
import com.example.collab.common.dto.UserDto
import com.example.collab.common.exception.ConflictException
import com.example.collab.common.exception.NotFoundException
import com.example.collab.common.exception.UnauthorizedException
import com.example.collab.common.security.JwtProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * Service class handling authentication business logic, registration, and user retrieval.
 *
 * @property userRepository Data repository for user operations
 * @property passwordEncoder Encoder for secure password hashing and verification
 * @property jwtProvider Provider utility for JWT creation and validation
 * @property jwtExpirationMs Configured token expiration duration in milliseconds
 */
@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtProvider: JwtProvider,
    @Value("\${app.jwt.expiration-ms}")
    private val jwtExpirationMs: Long
) {

    /**
     * Registers a new user account.
     *
     * @param request Registration payload containing username, email, and password
     * @return [AuthResponse] containing generated token and user info
     * @throws ConflictException if username or email is already registered
     */
    @Transactional
    fun register(request: RegisterRequest): AuthResponse {
        if (userRepository.existsByUsername(request.username)) {
            throw ConflictException("Username is already taken: ${request.username}")
        }
        if (userRepository.existsByEmail(request.email)) {
            throw ConflictException("Email is already registered: ${request.email}")
        }

        val newUser = User(
            username = request.username,
            email = request.email,
            password = passwordEncoder.encode(request.password)
        )
        val savedUser = userRepository.save(newUser)

        val token = jwtProvider.generateToken(savedUser.username, savedUser.role.name)
        return AuthResponse(
            token = token,
            username = savedUser.username,
            expiresIn = jwtExpirationMs
        )
    }

    /**
     * Authenticates an existing user and returns a JWT session token.
     *
     * @param request Login credentials containing username and password
     * @return [AuthResponse] containing generated token and user info
     * @throws UnauthorizedException if username is not found or password does not match
     */
    @Transactional(readOnly = true)
    fun login(request: LoginRequest): AuthResponse {
        val user = userRepository.findByUsername(request.username)
            ?: throw UnauthorizedException("Invalid username or password")

        if (!passwordEncoder.matches(request.password, user.password)) {
            throw UnauthorizedException("Invalid username or password")
        }

        val token = jwtProvider.generateToken(user.username, user.role.name)
        return AuthResponse(
            token = token,
            username = user.username,
            expiresIn = jwtExpirationMs
        )
    }

    /**
     * Fetches user profile data by ID.
     *
     * @param id Unique user UUID
     * @return [UserDto] containing public user information
     * @throws NotFoundException if user is not found
     */
    @Transactional(readOnly = true)
    fun getUserById(id: UUID): UserDto {
        val user = userRepository.findById(id).orElseThrow {
            NotFoundException("User not found with id: $id")
        }
        return mapToUserDto(user)
    }

    /**
     * Fetches user profile data by username.
     *
     * @param username Target username
     * @return [UserDto] containing public user information
     * @throws NotFoundException if user is not found
     */
    @Transactional(readOnly = true)
    fun getUserByUsername(username: String): UserDto {
        val user = userRepository.findByUsername(username)
            ?: throw NotFoundException("User not found with username: $username")
        return mapToUserDto(user)
    }

    private fun mapToUserDto(user: User): UserDto {
        return UserDto(
            id = user.id.toString(),
            username = user.username,
            email = user.email,
            displayName = user.displayName,
            role = user.role.name
        )
    }
}
