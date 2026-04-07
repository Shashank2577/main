package com.openclaw.ai.data.repository

import com.openclaw.ai.data.db.entity.SpaceEntity
import kotlinx.coroutines.flow.Flow

interface SpaceRepository {

    fun getAllSpaces(): Flow<List<SpaceEntity>>

    suspend fun getSpace(id: String): SpaceEntity?

    suspend fun createSpace(
        name: String,
        emoji: String = "\uD83D\uDCC1",
        description: String = "",
        systemPrompt: String? = null,
    ): SpaceEntity

    suspend fun updateSpace(space: SpaceEntity)

    suspend fun deleteSpace(id: String)

    suspend fun ensureDefaultSpace(): SpaceEntity
}
