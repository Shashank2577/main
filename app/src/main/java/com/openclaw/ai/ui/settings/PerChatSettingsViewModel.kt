package com.openclaw.ai.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openclaw.ai.data.db.entity.PerChatSettingsEntity
import com.openclaw.ai.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val DEFAULT_TEMPERATURE = 0.7f
private const val DEFAULT_TOP_K = 40
private const val DEFAULT_TOP_P = 0.95f
private const val DEFAULT_MAX_TOKENS = 4096

@HiltViewModel
class PerChatSettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _temperature = MutableStateFlow(DEFAULT_TEMPERATURE)
    val temperature: StateFlow<Float> = _temperature.asStateFlow()

    private val _topK = MutableStateFlow(DEFAULT_TOP_K)
    val topK: StateFlow<Int> = _topK.asStateFlow()

    private val _topP = MutableStateFlow(DEFAULT_TOP_P)
    val topP: StateFlow<Float> = _topP.asStateFlow()

    private val _maxTokens = MutableStateFlow(DEFAULT_MAX_TOKENS)
    val maxTokens: StateFlow<Int> = _maxTokens.asStateFlow()

    private val _isSaved = MutableStateFlow(false)
    val isSaved: StateFlow<Boolean> = _isSaved.asStateFlow()

    fun loadSettings(conversationId: String) {
        viewModelScope.launch {
            val existing = settingsRepository.getPerChatSettings(conversationId)
            if (existing != null) {
                _temperature.value = existing.temperature
                _topK.value = existing.topK
                _topP.value = existing.topP
                _maxTokens.value = existing.maxTokens
            } else {
                resetToDefaults()
            }
        }
    }

    fun saveSettings(conversationId: String) {
        viewModelScope.launch {
            settingsRepository.savePerChatSettings(
                PerChatSettingsEntity(
                    conversationId = conversationId,
                    temperature = _temperature.value,
                    topK = _topK.value,
                    topP = _topP.value,
                    maxTokens = _maxTokens.value,
                )
            )
            _isSaved.value = true
        }
    }

    fun resetToDefaults() {
        _temperature.value = DEFAULT_TEMPERATURE
        _topK.value = DEFAULT_TOP_K
        _topP.value = DEFAULT_TOP_P
        _maxTokens.value = DEFAULT_MAX_TOKENS
    }

    fun setTemperature(value: Float) {
        _temperature.value = value
    }

    fun setTopK(value: Int) {
        _topK.value = value
    }

    fun setTopP(value: Float) {
        _topP.value = value
    }

    fun setMaxTokens(value: Int) {
        _maxTokens.value = value
    }

    fun clearSaved() {
        _isSaved.value = false
    }
}
