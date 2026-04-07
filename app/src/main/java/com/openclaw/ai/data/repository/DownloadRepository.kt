package com.openclaw.ai.data.repository

import com.openclaw.ai.data.model.ModelInfo
import java.util.UUID

data class DownloadProgress(
    val modelId: String,
    val totalBytes: Long = 0,
    val receivedBytes: Long = 0,
    val bytesPerSecond: Long = 0,
    val isComplete: Boolean = false,
    val isFailed: Boolean = false,
    val errorMessage: String = "",
)

interface DownloadRepository {

    fun downloadModel(
        model: ModelInfo,
        onStatusUpdated: (DownloadProgress) -> Unit,
    ): UUID

    fun cancelDownload(model: ModelInfo)

    fun cancelAll(onComplete: () -> Unit)

    fun observeDownload(
        workerId: UUID,
        model: ModelInfo,
        onStatusUpdated: (DownloadProgress) -> Unit,
    )
}
