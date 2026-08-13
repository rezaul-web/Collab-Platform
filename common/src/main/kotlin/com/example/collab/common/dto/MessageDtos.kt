package com.example.collab.common.dto

/**
 * Data transfer object for sending a message to a room.
 *
 * @property roomId Identifier of the destination room
 * @property content Content of the message
 */
data class SendMessageRequest(
    val roomId: String,
    val content: String
)

/**
 * Data transfer object representing a stored message.
 *
 * @property id Unique message identifier
 * @property content Message text content
 * @property sentAt Timestamp when message was sent
 * @property senderUsername Username of the message sender
 * @property roomId Identifier of the room
 */
data class MessageDto(
    val id: String,
    val content: String,
    val sentAt: String,
    val senderUsername: String,
    val roomId: String
)

/**
 * Data payload for real-time chat messages.
 *
 * @property from Sender's username or identifier
 * @property content Message body text
 */
data class ChatMessagePayload(
    val from: String,
    val content: String
)

/**
 * Real-time response payload for broadcasted chat messages.
 *
 * @property from Sender's username or identifier
 * @property content Message body text
 * @property timestamp ISO timestamp of the message
 */
data class ChatMessageResponse(
    val from: String,
    val content: String,
    val timestamp: String
)

/**
 * Real-time event representing user presence status change.
 *
 * @property userId Unique user identifier
 * @property username User's username
 * @property type Presence status type
 */
data class PresenceEvent(
    val userId: String,
    val username: String,
    val type: PresenceType
)

/**
 * Enum representing user presence status.
 */
enum class PresenceType {
    ONLINE,
    OFFLINE,
    TYPING
}
