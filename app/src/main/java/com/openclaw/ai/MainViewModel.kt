package com.openclaw.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openclaw.ai.data.repository.SettingsRepository
import com.openclaw.ai.data.repository.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    // Tri-state: null = not yet checked, false = not complete, true = complete
    private val _onboardingComplete = MutableStateFlow<Boolean?>(null)
    val onboardingComplete: StateFlow<Boolean?> = _onboardingComplete.asStateFlow()

    val themeMode: StateFlow<ThemeMode> = settingsRepository.getThemeMode()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ThemeMode.SYSTEM,
        )

    private val _currentConversationId = MutableStateFlow<String?>(null)
    val currentConversationId: StateFlow<String?> = _currentConversationId.asStateFlow()

    private val _currentSpaceId = MutableStateFlow<String?>(null)
    val currentSpaceId: StateFlow<String?> = _currentSpaceId.asStateFlow()

    init {
        viewModelScope.launch {
            _onboardingComplete.value = settingsRepository.isOnboardingComplete()
        }
    }

    fun onOnboardingComplete() {
        viewModelScope.launch {
            settingsRepository.setOnboardingComplete(true)
            _onboardingComplete.value = true
        }
    }

    fun setCurrentConversation(conversationId: String?) {
        _currentConversationId.value = conversationId
    }

    fun setCurrentSpace(spaceId: String?) {
        _currentSpaceId.value = spaceId
    }
}
