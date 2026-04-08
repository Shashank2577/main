package com.openclaw.ai.di

import android.content.Context
import com.openclaw.ai.AppLifecycleProvider
import com.openclaw.ai.OpenClawLifecycleProvider
import com.openclaw.ai.data.DataStoreRepository
import com.openclaw.ai.data.DefaultDataStoreRepository
import com.openclaw.ai.data.DefaultDownloadRepository
import com.openclaw.ai.data.DownloadRepository as OpenClawDownloadRepository
import com.openclaw.ai.data.repository.*
import com.openclaw.ai.data.repository.impl.*
import com.openclaw.ai.runtime.LiteRtModelHelper
import com.openclaw.ai.runtime.LlmModelHelper
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindSpaceRepository(impl: SpaceRepositoryImpl): SpaceRepository

    @Binds
    @Singleton
    abstract fun bindConversationRepository(impl: ConversationRepositoryImpl): ConversationRepository

    @Binds
    @Singleton
    abstract fun bindModelRepository(impl: ModelRepositoryImpl): ModelRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindLlmModelHelper(impl: LiteRtModelHelper): LlmModelHelper

    @Binds
    @Singleton
    abstract fun bindAppLifecycleProvider(impl: OpenClawLifecycleProvider): AppLifecycleProvider

    @Module
    @InstallIn(SingletonComponent::class)
    object Providers {
        @Provides
        @Singleton
        fun provideDataStoreRepository(
            settingsDataStore: androidx.datastore.core.DataStore<com.openclaw.ai.proto.Settings>,
            userDataDataStore: androidx.datastore.core.DataStore<com.openclaw.ai.proto.UserData>,
            cutoutDataStore: androidx.datastore.core.DataStore<com.openclaw.ai.proto.CutoutCollection>,
            benchmarkResultsDataStore: androidx.datastore.core.DataStore<com.openclaw.ai.proto.BenchmarkResults>,
            skillsDataStore: androidx.datastore.core.DataStore<com.openclaw.ai.proto.Skills>,
        ): DataStoreRepository {
            return DefaultDataStoreRepository(
                settingsDataStore,
                userDataDataStore,
                cutoutDataStore,
                benchmarkResultsDataStore,
                skillsDataStore
            )
        }

        @Provides
        @Singleton
        fun provideDownloadRepository(
            @ApplicationContext context: Context,
            lifecycleProvider: AppLifecycleProvider,
        ): OpenClawDownloadRepository {
            return DefaultDownloadRepository(context, lifecycleProvider)
        }
    }
}
