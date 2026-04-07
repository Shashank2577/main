package com.openclaw.ai.data.repository.impl

import com.openclaw.ai.data.db.dao.SpaceDao
import com.openclaw.ai.data.db.entity.SpaceEntity
import com.openclaw.ai.data.repository.SpaceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpaceRepositoryImpl @Inject constructor(
    private val spaceDao: SpaceDao,
) : SpaceRepository {

    override fun getAllSpaces(): Flow<List<SpaceEntity>> =
        spaceDao.getAllSpaces()

    override suspend fun getSpace(id: String): SpaceEntity? =
        spaceDao.getSpace(id)

    override suspend fun createSpace(
        name: String,
        emoji: String,
        description: String,
        systemPrompt: String?,
    ): SpaceEntity {
        val space = SpaceEntity(
            id = UUID.randomUUID().toString(),
            name = name,
            emoji = emoji,
            description = description,
            systemPrompt = systemPrompt,
            createdAt = System.currentTimeMillis(),
        )
        spaceDao.insert(space)
        return space
    }

    override suspend fun updateSpace(space: SpaceEntity) {
        spaceDao.update(space)
    }

    override suspend fun deleteSpace(id: String) {
        spaceDao.delete(id)
    }

    override suspend fun ensureDefaultSpace(): SpaceEntity {
        if (spaceDao.count() == 0) {
            return createSpace(name = "General", emoji = "\uD83D\uDCC1")
        }
        return checkNotNull(spaceDao.getAllSpaces().first().firstOrNull()) {
            "No spaces found after ensureDefaultSpace"
        }
    }
}
