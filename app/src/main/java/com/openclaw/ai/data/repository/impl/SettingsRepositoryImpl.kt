package com.openclaw.ai.data.repository.impl

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.openclaw.ai.data.db.dao.PerChatSettingsDao
import com.openclaw.ai.data.db.entity.PerChatSettingsEntity
import com.openclaw.ai.data.repository.SettingsRepository
import com.openclaw.ai.data.repository.ThemeMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "openclaw_settings")

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val perChatSettingsDao: PerChatSettingsDao,
) : SettingsRepository {

    private val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
    private val KEY_DEFAULT_MODEL_ID = stringPreferencesKey("default_model_id")
    private val KEY_ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")

    private val ENCRYPTED_PREFS_FILE = "openclaw_secure_prefs"
    private val KEY_GEMINI_API_KEY = "gemini_api_key"

    private val encryptedPrefs by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            ENCRYPTED_PREFS_FILE,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    override fun getThemeMode(): Flow<ThemeMode> =
        context.dataStore.data.map { prefs ->
            when (prefs[KEY_THEME_MODE]) {
                "LIGHT" -> ThemeMode.LIGHT
                "DARK" -> ThemeMode.DARK
                else -> ThemeMode.SYSTEM
            }
        }

    override suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[KEY_THEME_MODE] = mode.name
        }
    }

    override suspend fun getGeminiApiKey(): String? =
        encryptedPrefs.getString(KEY_GEMINI_API_KEY, null)

    override suspend fun setGeminiApiKey(key: String?) {
        if (key == null) {
            encryptedPrefs.edit().remove(KEY_GEMINI_API_KEY).apply()
        } else {
            encryptedPrefs.edit().putString(KEY_GEMINI_API_KEY, key).apply()
        }
    }

    override suspend fun hasGeminiApiKey(): Boolean =
        !encryptedPrefs.getString(KEY_GEMINI_API_KEY, null).isNullOrBlank()

    override fun getDefaultModelId(): Flow<String> =
        context.dataStore.data.map { prefs ->
            prefs[KEY_DEFAULT_MODEL_ID] ?: "gemma-4-2b"
        }

    override suspend fun setDefaultModelId(modelId: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_DEFAULT_MODEL_ID] = modelId
        }
    }

    override suspend fun isOnboardingComplete(): Boolean =
        context.dataStore.data.first()[KEY_ONBOARDING_COMPLETE] ?: false

    override suspend fun setOnboardingComplete(complete: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ONBOARDING_COMPLETE] = complete
        }
    }

    override suspend fun getPerChatSettings(conversationId: String): PerChatSettingsEntity? =
        perChatSettingsDao.getSettings(conversationId)

    override fun observePerChatSettings(conversationId: String): Flow<PerChatSettingsEntity?> =
        perChatSettingsDao.observeSettings(conversationId)

    override suspend fun savePerChatSettings(settings: PerChatSettingsEntity) {
        perChatSettingsDao.upsert(settings)
    }

    override suspend fun deletePerChatSettings(conversationId: String) {
        perChatSettingsDao.delete(conversationId)
    }
}
