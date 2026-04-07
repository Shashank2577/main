package com.openclaw.ai.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.openclaw.ai.data.db.entity.SpaceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SpaceDao {

    @Query("SELECT * FROM spaces ORDER BY sortOrder ASC, lastUsedAt DESC")
    fun getAllSpaces(): Flow<List<SpaceEntity>>

    @Query("SELECT * FROM spaces WHERE id = :id")
    suspend fun getSpace(id: String): SpaceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(space: SpaceEntity)

    @Update
    suspend fun update(space: SpaceEntity)

    @Query("UPDATE spaces SET lastUsedAt = :timestamp WHERE id = :id")
    suspend fun updateLastUsedAt(id: String, timestamp: Long)

    @Query("DELETE FROM spaces WHERE id = :id")
    suspend fun delete(id: String)

    @Query("SELECT COUNT(*) FROM spaces")
    suspend fun count(): Int
}
