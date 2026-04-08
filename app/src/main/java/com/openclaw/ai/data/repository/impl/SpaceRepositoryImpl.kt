package com.openclaw.ai.data.repository.impl

import com.openclaw.ai.data.db.dao.SpaceDao
import com.openclaw.ai.data.db.entity.SpaceEntity
import com.openclaw.ai.data.repository.SpaceRepository
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpaceRepositoryImpl @Inject constructor(
    private val spaceDao: SpaceDao
) : SpaceRepository {
    override fun getAllSpaces(): Flow<List<SpaceEntity>> = spaceDao.getAllSpaces()

    override suspend fun createSpace(name: String) {
        spaceDao.insertSpace(SpaceEntity(id = UUID.randomUUID().toString(), name = name))
    }
}
