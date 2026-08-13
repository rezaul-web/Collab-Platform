package com.example.collab.common.dto

/**
 * Data transfer object for room creation request.
 *
 * @property name Name of the collaboration room
 * @property description Optional room description
 */
data class CreateRoomRequest(
    val name: String,
    val description: String? = null
)

/**
 * Data transfer object representing room details.
 *
 * @property id Unique room identifier
 * @property name Room name
 * @property description Optional room description
 * @property createdAt Timestamp when room was created
 * @property active Indicates whether the room is currently active
 * @property participantCount Current number of active participants in the room
 */
data class RoomDto(
    val id: String,
    val name: String,
    val description: String?,
    val createdAt: String,
    val active: Boolean,
    val participantCount: Int
)

/**
 * Data transfer object for joining a room.
 *
 * @property userId Identifier of the user joining the room
 */
data class JoinRoomRequest(
    val userId: String
)
