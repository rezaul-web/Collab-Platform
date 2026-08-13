package com.example.collab.auth.repository

import com.example.collab.auth.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

/**
 * Spring Data JPA repository for [User] entities.
 */
@Repository
interface UserRepository : JpaRepository<User, UUID> {
    /**
     * Retrieves a user by their username.
     *
     * @param username The username to look up
     * @return [User] if found, null otherwise
     */
    fun findByUsername(username: String): User?

    /**
     * Retrieves a user by their email.
     *
     * @param email The email to look up
     * @return [User] if found, null otherwise
     */
    fun findByEmail(email: String): User?

    /**
     * Checks if a user exists with the specified username.
     *
     * @param username Username to check
     * @return true if exists, false otherwise
     */
    fun existsByUsername(username: String): Boolean

    /**
     * Checks if a user exists with the specified email.
     *
     * @param email Email to check
     * @return true if exists, false otherwise
     */
    fun existsByEmail(email: String): Boolean
}
