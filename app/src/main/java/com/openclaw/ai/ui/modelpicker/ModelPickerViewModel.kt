package com.openclaw.ai.ui.modelpicker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openclaw.ai.data.model.ModelDownloadStatus
import com.openclaw.ai.data.model.ModelInfo
import com.openclaw.ai.data.repository.DownloadRepository
import com.openclaw.ai.data.repository.ModelRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ModelPickerViewModel @Inject constructor(
    private val modelRepository: ModelRepository,
) : ViewModel() {

    val localModels: StateFlow<List<ModelInfo>> = modelRepository.availableModels
        .map { models -> models.filter { it.isLocal } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    val cloudModels: StateFlow<List<ModelInfo>> = modelRepository.availableModels
        .map { models -> models.filter { it.isCloud } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    val activeModelId: StateFlow<String?> = modelRepository.activeModel
        .map { it?.id }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null,
        )

    val downloadStatuses: StateFlow<Map<String, ModelDownloadStatus>> =
        modelRepository.downloadStatuses

    val downloadProgress: StateFlow<Map<String, Float>> =
        modelRepository.downloadProgress

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun selectModel(modelId: String) {
        viewModelScope.launch {
            val status = downloadStatuses.value[modelId]
            val model = modelRepository.getModel(modelId) ?: return@launch
            if (model.isCloud || status == ModelDownloadStatus.DOWNLOADED) {
                modelRepository.setActiveModel(modelId)
            } else {
                downloadModel(modelId)
            }
        }
    }

    fun downloadModel(modelId: String) {
        viewModelScope.launch {
            val model = modelRepository.getModel(modelId) ?: return@launch
            modelRepository.downloadModel(
                model = model,
                onError = { error -> _errorMessage.value = error },
            )
        }
    }

    fun cancelDownload(modelId: String) {
        viewModelScope.launch {
            modelRepository.cancelDownload(modelId)
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
