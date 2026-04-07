package com.openclaw.ai.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "conversations",
    foreignKeys = [
        ForeignKey(
            entity = SpaceEntity::class,
            parentColumns = ["id"],
            childColumns = ["spaceId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("spaceId")]
)
data class ConversationEntity(
    @PrimaryKey
    val id: String,
    val spaceId: String,
    val title: String,
    val createdAt: Long,
    val lastMessageAt: Long,
    val systemPrompt: String? = null,
)
