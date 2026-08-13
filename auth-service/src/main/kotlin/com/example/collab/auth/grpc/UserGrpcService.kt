package com.example.collab.auth.grpc

import com.example.collab.auth.repository.UserRepository
import com.example.collab.common.security.JwtProvider
import com.example.collab.proto.user.GetUserRequest
import com.example.collab.proto.user.TokenRequest
import com.example.collab.proto.user.TokenResponse
import com.example.collab.proto.user.UserResponse
import com.example.collab.proto.user.UserServiceGrpcKt
import io.grpc.Status
import io.grpc.StatusException
import net.devh.boot.grpc.server.service.GrpcService
import java.util.UUID

/**
 * gRPC service implementation for user retrieval and JWT token validation over gRPC.
 *
 * Implements [UserServiceGrpcKt.UserServiceCoroutineImplBase] generated from `user.proto`.
 *
 * @property userRepository Repository for user data queries
 * @property jwtProvider Provider for JWT token parsing and validation
 */
@GrpcService
class UserGrpcService(
    private val userRepository: UserRepository,
    private val jwtProvider: JwtProvider
) : UserServiceGrpcKt.UserServiceCoroutineImplBase() {

    /**
     * gRPC handler to get user details by UUID string.
     *
     * @param request [GetUserRequest] containing target `user_id`
     * @return [UserResponse] with user details
     * @throws StatusException INVALID_ARGUMENT if user_id format is invalid or NOT_FOUND if user does not exist
     */
    override suspend fun getUser(request: GetUserRequest): UserResponse {
        val uuid = try {
            UUID.fromString(request.userId)
        } catch (e: IllegalArgumentException) {
            throw StatusException(Status.INVALID_ARGUMENT.withDescription("Invalid UUID format: ${request.userId}"))
        }

        val user = userRepository.findById(uuid).orElse(null)
            ?: throw StatusException(Status.NOT_FOUND.withDescription("User not found with id: ${request.userId}"))

        return UserResponse.newBuilder()
            .setUserId(user.id.toString())
            .setUsername(user.username)
            .setEmail(user.email)
            .setDisplayName(user.displayName ?: "")
            .setRole(user.role.name)
            .build()
    }

    /**
     * gRPC handler to validate a JWT token and extract token subject/role claims.
     *
     * @param request [TokenRequest] containing token string
     * @return [TokenResponse] indicating validity status, username, and role
     */
    override suspend fun validateToken(request: TokenRequest): TokenResponse {
        val isValid = jwtProvider.validateToken(request.token)

        if (!isValid) {
            return TokenResponse.newBuilder()
                .setValid(false)
                .setUsername("")
                .setRole("")
                .build()
        }

        val username = jwtProvider.getUsernameFromToken(request.token)
        val role = jwtProvider.getRoleFromToken(request.token)

        return TokenResponse.newBuilder()
            .setValid(true)
            .setUsername(username)
            .setRole(role)
            .build()
    }
}
