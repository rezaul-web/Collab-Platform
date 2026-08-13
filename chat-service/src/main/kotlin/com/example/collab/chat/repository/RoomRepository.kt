package com.example.collab.chat.repository

import com.example.collab.chat.model.Room
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID

/**
 * Repository interface for managing [Room] entity persistence operations.
 */
@Repository
interface RoomRepository : JpaRepository<Room, UUID> {

    /**
     * Finds a chat room by its unique name.
     *
     * @param name Unique name of the room
     * @return Optional containing the room if found, empty otherwise
     */
    fun findByName(name: String): Optional<Room>

    /**
     * Finds all active chat rooms.
     *
     * @return List of active room entities
     */
    fun findByActiveTrue(): List<Room>
}
