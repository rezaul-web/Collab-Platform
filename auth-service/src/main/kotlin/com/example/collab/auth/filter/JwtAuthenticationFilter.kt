package com.example.collab.auth.filter

import com.example.collab.common.security.JwtProvider
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

/**
 * Filter extending [OncePerRequestFilter] to validate Bearer JWT tokens in request headers
 * and populate the Spring Security Context with authentication details.
 *
 * @property jwtProvider Helper component for JWT token parsing and validation
 */
@Component
class JwtAuthenticationFilter(
    private val jwtProvider: JwtProvider
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authHeader = request.getHeader("Authorization")

        if (!authHeader.isNullOrBlank() && authHeader.startsWith("Bearer ")) {
            val token = authHeader.substring(7)

            if (jwtProvider.validateToken(token)) {
                val username = jwtProvider.getUsernameFromToken(token)
                val role = jwtProvider.getRoleFromToken(token)

                val authorityName = if (role.startsWith("ROLE_")) role else "ROLE_$role"
                val authorities = listOf(SimpleGrantedAuthority(authorityName))

                val authentication = UsernamePasswordAuthenticationToken(username, null, authorities)
                authentication.details = WebAuthenticationDetailsSource().buildDetails(request)

                SecurityContextHolder.getContext().authentication = authentication
            }
        }

        filterChain.doFilter(request, response)
    }
}
