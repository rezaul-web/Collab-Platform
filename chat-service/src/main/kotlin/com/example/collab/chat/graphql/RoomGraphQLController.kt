package com.example.collab.chat.graphql

import com.example.collab.chat.service.ChatService
import com.example.collab.common.dto.CreateRoomRequest
import com.example.collab.common.dto.MessageDto
import com.example.collab.common.dto.RoomDto
import org.slf4j.LoggerFactory
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.graphql.data.method.annotation.SubscriptionMapping
import org.springframework.stereotype.Controller
import reactor.core.publisher.Flux
import reactor.core.publisher.Sinks
import java.util.UUID

/**
 * Controller providing GraphQL API handlers for room queries, message operations, and real-time subscriptions.
 *
 * @property chatService Service powering room and message management
 */
@Controller
class RoomGraphQLController(
    private val chatService: ChatService
) {
    private val log = LoggerFactory.getLogger(RoomGraphQLController::class.java)

    private val messageSink: Sinks.Many<MessageDto> = Sinks.many().multicast().onBackpressureBuffer()

    /**
     * GraphQL Query mapping to retrieve all active chat rooms.
     *
     * @return List of active rooms
     */
    @QueryMapping
    fun rooms(): List<RoomDto> {
        return chatService.getActiveRooms()
    }

    /**
     * GraphQL Query mapping to retrieve details for a specific room.
     *
     * @param id Room UUID string
     * @return Room details or null if not found
     */
    @QueryMapping
    fun room(@Argument id: String): RoomDto? {
        return try {
            chatService.getRoom(UUID.fromString(id))
        } catch (e: Exception) {
            log.warn("Room not found for id {}: {}", id, e.message)
            null
        }
    }

    /**
     * GraphQL Query mapping to retrieve paginated messages for a room.
     *
     * @param roomId Room UUID string
     * @param page Zero-based page index, defaults to 0
     * @param size Page size, defaults to 50
     * @return List of messages in the specified room
     */
    @QueryMapping
    fun messages(
        @Argument roomId: String,
        @Argument page: Int?,
        @Argument size: Int?
    ): List<MessageDto> {
        val p = page ?: 0
        val s = size ?: 50
        return chatService.getMessages(UUID.fromString(roomId), p, s)
    }

    /**
     * GraphQL Mutation mapping to create a new chat room.
     *
     * @param name Room name
     * @param description Optional description text
     * @return Created room payload
     */
    @MutationMapping
    fun createRoom(
        @Argument name: String,
        @Argument description: String?
    ): RoomDto {
        val request = CreateRoomRequest(name = name, description = description)
        return chatService.createRoom(request)
    }

    /**
     * GraphQL Mutation mapping to send a message to a room and broadcast to reactive subscribers.
     *
     * @param roomId Target room UUID string
     * @param senderUsername Sender's username
     * @param content Message content string
     * @return Sent message payload
     */
    @MutationMapping
    fun sendMessage(
        @Argument roomId: String,
        @Argument senderUsername: String,
        @Argument content: String
    ): MessageDto {
        val message = chatService.sendMessage(UUID.fromString(roomId), senderUsername, content)
        messageSink.tryEmitNext(message)
        return message
    }

    /**
     * GraphQL Subscription mapping to stream room message updates in real-time.
     *
     * @param roomId Target room UUID string to filter updates for
     * @return Reactive [Flux] of message updates for the room
     */
    @SubscriptionMapping
    fun roomUpdates(@Argument roomId: String): Flux<MessageDto> {
        return messageSink.asFlux().filter { it.roomId == roomId }
    }
}
