package com.example.collab.auth.controller

import com.example.collab.auth.service.AuthService
import com.example.collab.common.dto.AuthResponse
import com.example.collab.common.dto.LoginRequest
import com.example.collab.common.dto.RegisterRequest
import com.example.collab.common.dto.UserDto
import com.example.collab.common.exception.UnauthorizedException
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * REST controller for authentication endpoints including registration, login, and current user profile retrieval.
 *
 * @property authService Business service for auth operations
 */
@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService
) {

    /**
     * Registers a new user.
     *
     * @param request Validated registration payload
     * @return [ResponseEntity] containing [AuthResponse] with HTTP status 201 CREATED
     */
    @PostMapping("/register")
    fun register(@RequestBody @Valid request: RegisterRequest): ResponseEntity<AuthResponse> {
        val response = authService.register(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    /**
     * Authenticates an existing user.
     *
     * @param request Validated login payload
     * @return [ResponseEntity] containing [AuthResponse] with HTTP status 200 OK
     */
    @PostMapping("/login")
    fun login(@RequestBody @Valid request: LoginRequest): ResponseEntity<AuthResponse> {
        val response = authService.login(request)
        return ResponseEntity.ok(response)
    }

    /**
     * Retrieves profile information of currently authenticated user.
     *
     * @param principal Currently authenticated principal (username)
     * @return [ResponseEntity] containing [UserDto] with HTTP status 200 OK
     */
    @GetMapping("/me")
    fun getCurrentUser(@AuthenticationPrincipal principal: Any?): ResponseEntity<UserDto> {
        if (principal == null) {
            throw UnauthorizedException("User is not authenticated")
        }
        val username = principal.toString()
        val userDto = authService.getUserByUsername(username)
        return ResponseEntity.ok(userDto)
    }
}
