package com.openclaw.ai.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = ConversationEntity::class,
            parentColumns = ["id"],
            childColumns = ["conversationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("conversationId")]
)
data class MessageEntity(
    @PrimaryKey
    val id: String,
    val conversationId: String,
    val role: String,       // "user", "assistant", "tool", "system"
    val content: String,
    val mediaUri: String? = null,
    val toolName: String? = null,
    val toolParams: String? = null,   // JSON
    val toolResult: String? = null,
    val timestamp: Long,
    val tokens: Int? = null,
)
