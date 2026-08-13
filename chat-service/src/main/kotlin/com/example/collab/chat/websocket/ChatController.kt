package com.example.collab.chat.websocket

import com.example.collab.chat.service.ChatService
import com.example.collab.common.dto.ChatMessagePayload
import com.example.collab.common.dto.ChatMessageResponse
import org.slf4j.LoggerFactory
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.stereotype.Controller
import java.time.Instant
import java.util.UUID

/**
 * Controller handling WebSocket STOMP messaging for real-time room chat.
 *
 * @property chatService Service used for persisting sent chat messages
 */
@Controller
class ChatController(
    private val chatService: ChatService
) {
    private val log = LoggerFactory.getLogger(ChatController::class.java)

    /**
     * Handles STOMP messages directed to `/chat/{roomId}`, persists the message,
     * and broadcasts the response to `/topic/chat/{roomId}`.
     *
     * @param roomId Target room ID destination variable
     * @param payload Incoming chat payload containing sender and content
     * @return [ChatMessageResponse] object to broadcast to subscribers
     */
    @MessageMapping("/chat/{roomId}")
    @SendTo("/topic/chat/{roomId}")
    fun handleChat(
        @DestinationVariable roomId: String,
        payload: ChatMessagePayload
    ): ChatMessageResponse {
        log.info("Received WebSocket message for room {}: {}", roomId, payload)

        try {
            val roomUuid = UUID.fromString(roomId)
            chatService.sendMessage(roomUuid, payload.from, payload.content)
        } catch (e: Exception) {
            log.error("Failed to persist message for room {}: {}", roomId, e.message)
        }

        val timestamp = Instant.now().toString()
        return ChatMessageResponse(
            from = payload.from,
            content = payload.content,
            timestamp = timestamp
        )
    }
}
