package com.example.collab.chat.service

import com.example.collab.chat.model.Message
import com.example.collab.chat.model.Room
import com.example.collab.chat.repository.MessageRepository
import com.example.collab.chat.repository.RoomRepository
import com.example.collab.common.dto.CreateRoomRequest
import com.example.collab.common.dto.MessageDto
import com.example.collab.common.dto.RoomDto
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

/**
 * Service class handling core business operations for rooms and messages.
 *
 * @property roomRepository Persistence repository for rooms
 * @property messageRepository Persistence repository for messages
 */
@Service
class ChatService(
    private val roomRepository: RoomRepository,
    private val messageRepository: MessageRepository
) {
    private val log = LoggerFactory.getLogger(ChatService::class.java)

    /**
     * Creates a new chat room.
     *
     * @param request Room creation request data
     * @param creatorId Optional identifier of the creator user
     * @return Created [RoomDto]
     */
    @Transactional
    fun createRoom(request: CreateRoomRequest, creatorId: String? = null): RoomDto {
        log.info("Creating room with name: {}", request.name)
        roomRepository.findByName(request.name).ifPresent {
            throw IllegalArgumentException("Room with name '${request.name}' already exists")
        }

        val room = Room(
            name = request.name,
            description = request.description,
            createdAt = Instant.now(),
            active = true,
            createdBy = creatorId
        )

        val savedRoom = roomRepository.save(room)
        return savedRoom.toDto()
    }

    /**
     * Retrieves a room by its unique identifier.
     *
     * @param id Unique UUID of the room
     * @return Found [RoomDto]
     */
    @Transactional(readOnly = true)
    fun getRoom(id: UUID): RoomDto {
        val room = roomRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Room not found with id: $id") }
        return room.toDto()
    }

    /**
     * Retrieves all active chat rooms.
     *
     * @return List of active [RoomDto] instances
     */
    @Transactional(readOnly = true)
    fun getActiveRooms(): List<RoomDto> {
        return roomRepository.findByActiveTrue().map { it.toDto() }
    }

    /**
     * Sends and persists a message to a room.
     *
     * @param roomId Target room UUID
     * @param senderUsername Username of the message sender
     * @param content Text body of the message
     * @return Persisted [MessageDto]
     */
    @Transactional
    fun sendMessage(roomId: UUID, senderUsername: String, content: String): MessageDto {
        log.info("Sending message to room {} by user {}", roomId, senderUsername)
        val room = roomRepository.findById(roomId)
            .orElseThrow { IllegalArgumentException("Room not found with id: $roomId") }

        val message = Message(
            content = content,
            sentAt = Instant.now(),
            senderUsername = senderUsername,
            room = room
        )

        val savedMessage = messageRepository.save(message)
        return savedMessage.toDto()
    }

    /**
     * Retrieves paginated messages for a specified room.
     *
     * @param roomId Target room UUID
     * @param page Zero-based page index
     * @param size Number of items per page
     * @return List of [MessageDto] items
     */
    @Transactional(readOnly = true)
    fun getMessages(roomId: UUID, page: Int, size: Int): List<MessageDto> {
        val pageable = PageRequest.of(page, size)
        return messageRepository.findByRoomIdOrderBySentAtDesc(roomId, pageable).map { it.toDto() }
    }

    private fun Room.toDto(participantCount: Int = 0): RoomDto {
        return RoomDto(
            id = this.id.toString(),
            name = this.name,
            description = this.description,
            createdAt = this.createdAt.toString(),
            active = this.active,
            participantCount = participantCount
        )
    }

    private fun Message.toDto(): MessageDto {
        return MessageDto(
            id = this.id.toString(),
            content = this.content,
            sentAt = this.sentAt.toString(),
            senderUsername = this.senderUsername,
            roomId = this.room.id.toString()
        )
    }
}
