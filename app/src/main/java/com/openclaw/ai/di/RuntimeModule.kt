package com.openclaw.ai.di

import com.openclaw.ai.runtime.LiteRtModelHelper
import com.openclaw.ai.runtime.LlmModelHelper
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Binds runtime layer interfaces.
 *
 * [LiteRtModelHelper], [GeminiModelHelper], and [ModelRouter] all carry
 * @Singleton @Inject constructors so Hilt provides them automatically.
 * This module only adds the [LlmModelHelper] interface binding so any
 * injection site that depends on the interface receives [LiteRtModelHelper]
 * as the default (on-device) implementation.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RuntimeModule {

    @Binds
    @Singleton
    abstract fun bindLlmModelHelper(impl: LiteRtModelHelper): LlmModelHelper
}
