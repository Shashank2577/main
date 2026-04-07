package com.openclaw.ai.data.repository

import com.openclaw.ai.data.db.entity.PerChatSettingsEntity
import kotlinx.coroutines.flow.Flow

enum class ThemeMode {
    LIGHT, DARK, SYSTEM
}

interface SettingsRepository {

    // Theme
    fun getThemeMode(): Flow<ThemeMode>
    suspend fun setThemeMode(mode: ThemeMode)

    // API Keys (encrypted)
    suspend fun getGeminiApiKey(): String?
    suspend fun setGeminiApiKey(key: String?)
    suspend fun hasGeminiApiKey(): Boolean

    // Default model
    fun getDefaultModelId(): Flow<String>
    suspend fun setDefaultModelId(modelId: String)

    // Onboarding
    suspend fun isOnboardingComplete(): Boolean
    suspend fun setOnboardingComplete(complete: Boolean)

    // Per-chat settings
    suspend fun getPerChatSettings(conversationId: String): PerChatSettingsEntity?
    fun observePerChatSettings(conversationId: String): Flow<PerChatSettingsEntity?>
    suspend fun savePerChatSettings(settings: PerChatSettingsEntity)
    suspend fun deletePerChatSettings(conversationId: String)
}
