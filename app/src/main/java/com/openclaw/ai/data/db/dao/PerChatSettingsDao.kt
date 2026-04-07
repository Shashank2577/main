package com.openclaw.ai.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.openclaw.ai.data.db.entity.PerChatSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PerChatSettingsDao {

    @Query("SELECT * FROM per_chat_settings WHERE conversationId = :conversationId")
    suspend fun getSettings(conversationId: String): PerChatSettingsEntity?

    @Query("SELECT * FROM per_chat_settings WHERE conversationId = :conversationId")
    fun observeSettings(conversationId: String): Flow<PerChatSettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(settings: PerChatSettingsEntity)

    @Query("DELETE FROM per_chat_settings WHERE conversationId = :conversationId")
    suspend fun delete(conversationId: String)
}
