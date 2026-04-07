package com.openclaw.ai.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "spaces")
data class SpaceEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val emoji: String = "\uD83D\uDCC1", // 📁
    val description: String = "",
    val systemPrompt: String? = null,
    val createdAt: Long,
    val lastUsedAt: Long = 0L,
    val sortOrder: Int = 0,
)
