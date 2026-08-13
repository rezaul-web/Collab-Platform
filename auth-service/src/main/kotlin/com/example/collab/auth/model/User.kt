package com.example.collab.auth.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

/**
 * User roles supported by the application.
 */
enum class UserRole {
    USER,
    ADMIN
}

/**
 * JPA entity representing user entity stored in the database.
 *
 * @property id Unique UUID primary key
 * @property username Unique user identifier used for login
 * @property email Unique contact email address
 * @property password Encrypted password hash
 * @property displayName Optional public display name
 * @property createdAt Timestamp when account was created
 * @property role Role assigned to user (USER or ADMIN)
 */
@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(unique = true, nullable = false)
    val username: String,

    @Column(unique = true, nullable = false)
    val email: String,

    @Column(nullable = false)
    val password: String,

    @Column(name = "display_name")
    val displayName: String? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val role: UserRole = UserRole.USER
)
