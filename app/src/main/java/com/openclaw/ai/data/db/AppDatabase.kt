package com.openclaw.ai.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.openclaw.ai.data.db.dao.ConversationDao
import com.openclaw.ai.data.db.dao.MessageDao
import com.openclaw.ai.data.db.dao.PerChatSettingsDao
import com.openclaw.ai.data.db.dao.SpaceDao
import com.openclaw.ai.data.db.entity.ConversationEntity
import com.openclaw.ai.data.db.entity.MessageEntity
import com.openclaw.ai.data.db.entity.PerChatSettingsEntity
import com.openclaw.ai.data.db.entity.SpaceEntity

@Database(
    entities = [
        ConversationEntity::class,
        MessageEntity::class,
        SpaceEntity::class,
        PerChatSettingsEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
    abstract fun spaceDao(): SpaceDao
    abstract fun perChatSettingsDao(): PerChatSettingsDao
}
