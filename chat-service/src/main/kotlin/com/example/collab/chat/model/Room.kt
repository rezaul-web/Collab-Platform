package com.example.collab.chat.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

/**
 * Entity representing a chat room within the collaboration platform.
 *
 * @property id Unique identifier for the room
 * @property name Unique name of the room
 * @property description Optional descriptive text for the room
 * @property createdAt Timestamp when the room was created
 * @property active Indicates whether the room is currently active
 * @property createdBy User ID of the user who created the room
 */
@Entity
@Table(name = "rooms")
class Room(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @Column(nullable = false, unique = true)
    var name: String = "",

    @Column(length = 500)
    var description: String? = null,

    @Column(nullable = false)
    var createdAt: Instant = Instant.now(),

    @Column(nullable = false)
    var active: Boolean = true,

    @Column
    var createdBy: String? = null
)
