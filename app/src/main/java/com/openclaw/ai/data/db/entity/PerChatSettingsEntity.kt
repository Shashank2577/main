package com.openclaw.ai.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "per_chat_settings",
    foreignKeys = [
        ForeignKey(
            entity = ConversationEntity::class,
            parentColumns = ["id"],
            childColumns = ["conversationId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PerChatSettingsEntity(
    @PrimaryKey
    val conversationId: String,
    val temperature: Float = 0.7f,
    val topK: Int = 40,
    val topP: Float = 0.95f,
    val maxTokens: Int = 4096,
    val preferredModelId: String? = null,
    val enabledTools: String? = null, // comma-separated tool names
)
