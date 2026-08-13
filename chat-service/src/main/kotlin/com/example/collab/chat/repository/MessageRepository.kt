package com.example.collab.chat.repository

import com.example.collab.chat.model.Message
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

/**
 * Repository interface for managing [Message] entity persistence operations.
 */
@Repository
interface MessageRepository : JpaRepository<Message, UUID> {

    /**
     * Retrieves messages for a specific room ordered by sent timestamp descending.
     *
     * @param roomId Identifier of the room
     * @param pageable Pagination configuration
     * @return List of matching messages
     */
    fun findByRoomIdOrderBySentAtDesc(roomId: UUID, pageable: Pageable): List<Message>

    /**
     * Counts total number of messages sent to a specific room.
     *
     * @param roomId Identifier of the room
     * @return Total count of messages
     */
    fun countByRoomId(roomId: UUID): Long
}
