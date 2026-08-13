package com.example.collab.chat.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

/**
 * Entity representing a chat message sent inside a room.
 *
 * @property id Unique identifier for the message
 * @property content Text body of the message
 * @property sentAt Timestamp when the message was sent
 * @property senderUsername Username of the sender
 * @property room Reference to the associated chat room
 */
@Entity
@Table(name = "messages")
class Message(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @Column(nullable = false, length = 4000)
    var content: String = "",

    @Column(nullable = false)
    var sentAt: Instant = Instant.now(),

    @Column(nullable = false)
    var senderUsername: String = "",

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    var room: Room = Room()
)
