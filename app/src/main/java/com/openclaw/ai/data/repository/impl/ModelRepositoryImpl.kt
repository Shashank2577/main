package com.openclaw.ai.data.repository.impl

import android.content.Context
import com.openclaw.ai.data.model.DefaultModels
import com.openclaw.ai.data.model.ModelDownloadStatus
import com.openclaw.ai.data.model.ModelInfo
import com.openclaw.ai.data.model.ModelType
import com.openclaw.ai.data.repository.DownloadProgress
import com.openclaw.ai.data.repository.DownloadRepository
import com.openclaw.ai.data.repository.ModelRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ModelRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val downloadRepository: DownloadRepository,
) : ModelRepository {

    private val _availableModels = MutableStateFlow(DefaultModels.ALL)
    override val availableModels: StateFlow<List<ModelInfo>> = _availableModels.asStateFlow()

    private val _activeModel = MutableStateFlow<ModelInfo?>(null)
    override val activeModel: StateFlow<ModelInfo?> = _activeModel.asStateFlow()

    private val _downloadStatuses = MutableStateFlow(
        DefaultModels.ALL.associate { model ->
            model.id to if (model.type == ModelType.LOCAL) ModelDownloadStatus.NOT_DOWNLOADED
                        else ModelDownloadStatus.DOWNLOADED
        }
    )
    override val downloadStatuses: StateFlow<Map<String, ModelDownloadStatus>> =
        _downloadStatuses.asStateFlow()

    private val _downloadProgress = MutableStateFlow<Map<String, Float>>(emptyMap())
    override val downloadProgress: StateFlow<Map<String, Float>> = _downloadProgress.asStateFlow()

    override fun getModel(id: String): ModelInfo? =
        _availableModels.value.firstOrNull { it.id == id }

    override fun getLocalModels(): List<ModelInfo> =
        _availableModels.value.filter { it.isLocal }

    override fun getCloudModels(): List<ModelInfo> =
        _availableModels.value.filter { it.isCloud }

    override fun getDownloadedModels(): List<ModelInfo> =
        _availableModels.value.filter { model ->
            _downloadStatuses.value[model.id] == ModelDownloadStatus.DOWNLOADED
        }

    override suspend fun setActiveModel(modelId: String) {
        _activeModel.value = getModel(modelId)
    }

    override suspend fun downloadModel(
        model: ModelInfo,
        onProgress: (Float) -> Unit,
        onComplete: () -> Unit,
        onError: (String) -> Unit,
    ) {
        updateStatus(model.id, ModelDownloadStatus.DOWNLOADING)

        downloadRepository.downloadModel(model) { progress: DownloadProgress ->
            when {
                progress.isFailed -> {
                    updateStatus(model.id, ModelDownloadStatus.FAILED)
                    onError(progress.errorMessage)
                }
                progress.isComplete -> {
                    updateStatus(model.id, ModelDownloadStatus.DOWNLOADED)
                    updateProgress(model.id, 1f)
                    onComplete()
                }
                else -> {
                    val fraction = if (progress.totalBytes > 0) {
                        progress.receivedBytes.toFloat() / progress.totalBytes.toFloat()
                    } else {
                        0f
                    }
                    updateProgress(model.id, fraction)
                    onProgress(fraction)
                }
            }
        }
    }

    override suspend fun cancelDownload(modelId: String) {
        val model = getModel(modelId) ?: return
        downloadRepository.cancelDownload(model)
        updateStatus(modelId, ModelDownloadStatus.NOT_DOWNLOADED)
        updateProgress(modelId, 0f)
    }

    override suspend fun deleteDownloadedModel(modelId: String) {
        val model = getModel(modelId) ?: return
        val file = modelFile(model.downloadFileName)
        if (file.exists()) {
            file.delete()
        }
        updateStatus(modelId, ModelDownloadStatus.NOT_DOWNLOADED)
        updateProgress(modelId, 0f)
        if (_activeModel.value?.id == modelId) {
            _activeModel.value = null
        }
    }

    override suspend fun isModelDownloaded(modelId: String): Boolean {
        val model = getModel(modelId) ?: return false
        return modelFile(model.downloadFileName).exists()
    }

    override fun observeDownloadProgress(modelId: String): Flow<Float> =
        _downloadProgress.map { it[modelId] ?: 0f }

    private fun modelFile(fileName: String): File =
        File(context.getExternalFilesDir(null), fileName)

    private fun updateStatus(modelId: String, status: ModelDownloadStatus) {
        _downloadStatuses.value = _downloadStatuses.value + (modelId to status)
    }

    private fun updateProgress(modelId: String, fraction: Float) {
        _downloadProgress.value = _downloadProgress.value + (modelId to fraction)
    }
}
