package com.example.collab.chat.websocket

import com.example.collab.common.dto.PresenceEvent
import org.slf4j.LoggerFactory
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.stereotype.Controller

/**
 * Controller handling user presence STOMP events (ONLINE, OFFLINE, TYPING).
 */
@Controller
class PresenceController {

    private val log = LoggerFactory.getLogger(PresenceController::class.java)

    /**
     * Receives presence event messages for a room and broadcasts them to `/topic/presence/{roomId}`.
     *
     * @param roomId Target room ID destination variable
     * @param event Presence event payload
     * @return Presence event to be broadcast to room listeners
     */
    @MessageMapping("/presence/{roomId}")
    @SendTo("/topic/presence/{roomId}")
    fun handlePresence(
        @DestinationVariable roomId: String,
        event: PresenceEvent
    ): PresenceEvent {
        log.info("Presence update in room {}: user {} status {}", roomId, event.username, event.type)
        return event
    }
}
