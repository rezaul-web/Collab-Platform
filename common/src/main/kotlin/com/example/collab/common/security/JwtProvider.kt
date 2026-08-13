package com.example.collab.common.security

import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.Date
import javax.crypto.SecretKey

/**
 * Component for generating, parsing, and validating JWT tokens.
 *
 * @property jwtSecret Base64-encoded secret key used for signing JWT tokens
 * @property jwtExpirationMs Expiration duration in milliseconds for generated tokens
 */
@Component
class JwtProvider(
    @Value("\${app.jwt.secret}")
    private val jwtSecret: String,
    @Value("\${app.jwt.expiration-ms}")
    private val jwtExpirationMs: Long
) {
    private val log = LoggerFactory.getLogger(JwtProvider::class.java)

    private val key: SecretKey by lazy {
        Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret))
    }

    /**
     * Generates a JWT token for the specified username and role.
     *
     * @param username The subject of the token
     * @param role The role granted to the user (defaults to "USER")
     * @return Signed JWT token string
     */
    fun generateToken(username: String, role: String = "USER"): String {
        val now = Date()
        val expiryDate = Date(now.time + jwtExpirationMs)
        return Jwts.builder()
            .subject(username)
            .claim("role", role)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(key)
            .compact()
    }

    /**
     * Extracts the username (subject) from a given JWT token.
     *
     * @param token JWT token string
     * @return Username extracted from the token
     */
    fun getUsernameFromToken(token: String): String {
        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
            .subject
    }

    /**
     * Extracts the role claim from a given JWT token.
     *
     * @param token JWT token string
     * @return Role extracted from token, defaulting to "USER" if absent
     */
    fun getRoleFromToken(token: String): String {
        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
            .get("role", String::class.java) ?: "USER"
    }

    /**
     * Validates the integrity and expiration of a JWT token.
     *
     * @param token JWT token string
     * @return True if token is valid, false otherwise
     */
    fun validateToken(token: String): Boolean {
        return try {
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
            true
        } catch (e: JwtException) {
            log.error("Invalid JWT token: {}", e.message)
            false
        } catch (e: IllegalArgumentException) {
            log.error("JWT claims string is empty: {}", e.message)
            false
        }
    }
}
