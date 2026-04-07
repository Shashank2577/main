package com.openclaw.ai.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openclaw.ai.data.model.DefaultModels
import com.openclaw.ai.data.model.ModelDownloadStatus
import com.openclaw.ai.data.repository.ModelRepository
import com.openclaw.ai.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingUiState(
    val currentStep: Int = 0,
    val isDownloading: Boolean = false,
    val downloadProgress: Float = 0f,
    val downloadEtaSeconds: Int? = null,
    val isComplete: Boolean = false,
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val modelRepository: ModelRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun goToStep(step: Int) {
        _uiState.value = _uiState.value.copy(currentStep = step.coerceIn(0, 3))
    }

    fun nextStep() {
        val next = _uiState.value.currentStep + 1
        if (next > 3) {
            completeOnboarding()
        } else {
            _uiState.value = _uiState.value.copy(currentStep = next)
        }
    }

    fun previousStep() {
        val prev = (_uiState.value.currentStep - 1).coerceAtLeast(0)
        _uiState.value = _uiState.value.copy(currentStep = prev)
    }

    fun startModelDownload() {
        val model = DefaultModels.GEMMA_4
        _uiState.value = _uiState.value.copy(isDownloading = true, downloadProgress = 0f)
        viewModelScope.launch {
            modelRepository.downloadModel(
                model = model,
                onProgress = { progress ->
                    _uiState.value = _uiState.value.copy(downloadProgress = progress)
                },
                onComplete = {
                    _uiState.value = _uiState.value.copy(
                        isDownloading = false,
                        downloadProgress = 1f,
                    )
                    nextStep()
                },
                onError = {
                    _uiState.value = _uiState.value.copy(isDownloading = false)
                },
            )
        }
    }

    fun skipModelDownload() {
        nextStep()
    }

    fun saveApiKey(key: String) {
        viewModelScope.launch {
            settingsRepository.setGeminiApiKey(key.trim().ifEmpty { null })
            completeOnboarding()
        }
    }

    fun skipCloudSetup() {
        completeOnboarding()
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            settingsRepository.setOnboardingComplete(true)
            _uiState.value = _uiState.value.copy(isComplete = true)
        }
    }

    fun cancelDownload() {
        viewModelScope.launch {
            val status = modelRepository.downloadStatuses.value[DefaultModels.GEMMA_4.id]
            if (status == ModelDownloadStatus.DOWNLOADING) {
                modelRepository.cancelDownload(DefaultModels.GEMMA_4.id)
            }
            _uiState.value = _uiState.value.copy(isDownloading = false, downloadProgress = 0f)
        }
    }
}
