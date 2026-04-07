package com.openclaw.ai.data.repository

import com.openclaw.ai.data.model.ModelDownloadStatus
import com.openclaw.ai.data.model.ModelInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface ModelRepository {

    val availableModels: StateFlow<List<ModelInfo>>

    val activeModel: StateFlow<ModelInfo?>

    val downloadStatuses: StateFlow<Map<String, ModelDownloadStatus>>

    val downloadProgress: StateFlow<Map<String, Float>>

    fun getModel(id: String): ModelInfo?

    fun getLocalModels(): List<ModelInfo>

    fun getCloudModels(): List<ModelInfo>

    fun getDownloadedModels(): List<ModelInfo>

    suspend fun setActiveModel(modelId: String)

    suspend fun downloadModel(
        model: ModelInfo,
        onProgress: (Float) -> Unit = {},
        onComplete: () -> Unit = {},
        onError: (String) -> Unit = {},
    )

    suspend fun cancelDownload(modelId: String)

    suspend fun deleteDownloadedModel(modelId: String)

    suspend fun isModelDownloaded(modelId: String): Boolean

    fun observeDownloadProgress(modelId: String): Flow<Float>
}
