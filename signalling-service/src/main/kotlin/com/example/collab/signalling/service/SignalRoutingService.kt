package com.example.collab.signalling.service

import com.example.collab.proto.signalling.SignalEvent
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Service managing room-based WebRTC signal routing and event storage.
 */
@Service
class SignalRoutingService {
    private val log = LoggerFactory.getLogger(SignalRoutingService::class.java)

    private val roomSignalsMap = ConcurrentHashMap<String, MutableList<SignalEvent>>()

    /**
     * Adds a WebRTC signalling event to the specified room.
     *
     * @param roomId The unique identifier of the room
     * @param event The WebRTC signal event to register
     */
    fun addSignal(roomId: String, event: SignalEvent) {
        log.debug("Adding signal event for room '{}' from user '{}'", roomId, event.fromUserId)
        roomSignalsMap.computeIfAbsent(roomId) { CopyOnWriteArrayList() }.add(event)
    }

    /**
     * Retrieves all recorded signalling events for a given room.
     *
     * @param roomId The unique identifier of the room
     * @return List of WebRTC signal events recorded for the room
     */
    fun getSignalsForRoom(roomId: String): List<SignalEvent> {
        return roomSignalsMap[roomId]?.toList() ?: emptyList()
    }

    /**
     * Clears all stored signalling events for the specified room.
     *
     * @param roomId The unique identifier of the room
     */
    fun clearRoom(roomId: String) {
        log.info("Clearing signal history for room '{}'", roomId)
        roomSignalsMap.remove(roomId)
    }
}
