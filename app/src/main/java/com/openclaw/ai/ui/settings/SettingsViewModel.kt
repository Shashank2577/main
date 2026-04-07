package com.openclaw.ai.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openclaw.ai.data.model.ModelDownloadStatus
import com.openclaw.ai.data.model.ModelInfo
import com.openclaw.ai.data.repository.ConversationRepository
import com.openclaw.ai.data.repository.ModelRepository
import com.openclaw.ai.data.repository.SettingsRepository
import com.openclaw.ai.data.repository.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.google.gson.GsonBuilder
import javax.inject.Inject

data class SettingsUiState(
    val geminiApiKey: String = "",
    val isApiKeyVisible: Boolean = false,
    val isApiKeyValid: Boolean? = null,
    val isTesting: Boolean = false,
    val showClearConversationsDialog: Boolean = false,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val modelRepository: ModelRepository,
    private val conversationRepository: ConversationRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    val availableModels: StateFlow<List<ModelInfo>> = modelRepository.availableModels
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    val downloadStatuses: StateFlow<Map<String, ModelDownloadStatus>> = modelRepository.downloadStatuses
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyMap(),
        )

    val downloadProgress: StateFlow<Map<String, Float>> = modelRepository.downloadProgress
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyMap(),
        )

    val themeMode: StateFlow<ThemeMode> = settingsRepository.getThemeMode()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ThemeMode.SYSTEM,
        )

    val defaultModelId: StateFlow<String> = settingsRepository.getDefaultModelId()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = "",
        )

    init {
        viewModelScope.launch {
            val key = settingsRepository.getGeminiApiKey() ?: ""
            _uiState.value = _uiState.value.copy(geminiApiKey = key)
        }
    }

    fun setTheme(mode: ThemeMode) {
        viewModelScope.launch {
            settingsRepository.setThemeMode(mode)
        }
    }

    fun setDefaultModel(modelId: String) {
        viewModelScope.launch {
            settingsRepository.setDefaultModelId(modelId)
        }
    }

    fun onApiKeyChange(key: String) {
        _uiState.value = _uiState.value.copy(
            geminiApiKey = key,
            isApiKeyValid = null,
        )
    }

    fun toggleApiKeyVisibility() {
        _uiState.value = _uiState.value.copy(
            isApiKeyVisible = !_uiState.value.isApiKeyVisible,
        )
    }

    fun saveApiKey(key: String) {
        viewModelScope.launch {
            settingsRepository.setGeminiApiKey(key.ifBlank { null })
            _uiState.value = _uiState.value.copy(geminiApiKey = key)
        }
    }

    fun testApiKey() {
        val key = _uiState.value.geminiApiKey
        if (key.isBlank()) {
            _uiState.value = _uiState.value.copy(isApiKeyValid = false)
            return
        }
        _uiState.value = _uiState.value.copy(isTesting = true, isApiKeyValid = null)
        viewModelScope.launch {
            // Persist first, then test via a lightweight models list request.
            settingsRepository.setGeminiApiKey(key)
            val isValid = withContext(Dispatchers.IO) {
                runCatching {
                    val url = "https://generativelanguage.googleapis.com/v1beta/models?key=$key"
                    val connection = java.net.URL(url).openConnection() as java.net.HttpURLConnection
                    connection.connectTimeout = 5_000
                    connection.readTimeout = 5_000
                    val code = connection.responseCode
                    connection.disconnect()
                    code == 200
                }.getOrDefault(false)
            }
            _uiState.value = _uiState.value.copy(isTesting = false, isApiKeyValid = isValid)
        }
    }

    fun deleteModel(modelId: String) {
        viewModelScope.launch {
            modelRepository.deleteDownloadedModel(modelId)
        }
    }

    fun downloadModel(modelId: String) {
        val model = modelRepository.getModel(modelId) ?: return
        viewModelScope.launch {
            modelRepository.downloadModel(model)
        }
    }

    fun cancelDownload(modelId: String) {
        viewModelScope.launch {
            modelRepository.cancelDownload(modelId)
        }
    }

    fun showClearConversationsDialog() {
        _uiState.value = _uiState.value.copy(showClearConversationsDialog = true)
    }

    fun dismissClearConversationsDialog() {
        _uiState.value = _uiState.value.copy(showClearConversationsDialog = false)
    }

    fun clearAllConversations() {
        viewModelScope.launch {
            conversationRepository.deleteAllConversations()
            _uiState.value = _uiState.value.copy(showClearConversationsDialog = false)
        }
    }

    fun exportConversations(onResult: (String) -> Unit) {
        viewModelScope.launch {
            val conversations = conversationRepository.getAllConversations().first()
            val gson = GsonBuilder().setPrettyPrinting().create()
            val export = gson.toJson(mapOf("conversations" to conversations))
            onResult(export)
        }
    }
}
