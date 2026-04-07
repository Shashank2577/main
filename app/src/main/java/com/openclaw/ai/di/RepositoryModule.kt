package com.openclaw.ai.di

import com.openclaw.ai.data.repository.ConversationRepository
import com.openclaw.ai.data.repository.DownloadRepository
import com.openclaw.ai.data.repository.ModelRepository
import com.openclaw.ai.data.repository.SettingsRepository
import com.openclaw.ai.data.repository.SpaceRepository
import com.openclaw.ai.data.repository.impl.ConversationRepositoryImpl
import com.openclaw.ai.data.repository.impl.DownloadRepositoryImpl
import com.openclaw.ai.data.repository.impl.ModelRepositoryImpl
import com.openclaw.ai.data.repository.impl.SettingsRepositoryImpl
import com.openclaw.ai.data.repository.impl.SpaceRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindConversationRepository(
        impl: ConversationRepositoryImpl,
    ): ConversationRepository

    @Binds
    @Singleton
    abstract fun bindSpaceRepository(
        impl: SpaceRepositoryImpl,
    ): SpaceRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        impl: SettingsRepositoryImpl,
    ): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindModelRepository(
        impl: ModelRepositoryImpl,
    ): ModelRepository

    @Binds
    @Singleton
    abstract fun bindDownloadRepository(
        impl: DownloadRepositoryImpl,
    ): DownloadRepository
}
