package com.openclaw.ai.di

import android.content.Context
import androidx.room.Room
import com.openclaw.ai.data.db.AppDatabase
import com.openclaw.ai.data.db.dao.ConversationDao
import com.openclaw.ai.data.db.dao.MessageDao
import com.openclaw.ai.data.db.dao.PerChatSettingsDao
import com.openclaw.ai.data.db.dao.SpaceDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "openclaw.db"
        ).build()
    }

    @Provides
    fun provideConversationDao(db: AppDatabase): ConversationDao = db.conversationDao()

    @Provides
    fun provideMessageDao(db: AppDatabase): MessageDao = db.messageDao()

    @Provides
    fun provideSpaceDao(db: AppDatabase): SpaceDao = db.spaceDao()

    @Provides
    fun providePerChatSettingsDao(db: AppDatabase): PerChatSettingsDao = db.perChatSettingsDao()
}
