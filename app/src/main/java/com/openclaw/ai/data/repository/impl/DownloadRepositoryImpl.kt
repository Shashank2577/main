package com.openclaw.ai.data.repository.impl

import android.content.Context
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.openclaw.ai.data.model.ModelInfo
import com.openclaw.ai.data.repository.DownloadProgress
import com.openclaw.ai.data.repository.DownloadRepository
import com.openclaw.ai.worker.DownloadWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : DownloadRepository {

    companion object {
        const val KEY_MODEL_URL = "model_url"
        const val KEY_MODEL_NAME = "model_name"
        const val KEY_MODEL_DOWNLOAD_FILE_NAME = "model_download_file_name"
        const val KEY_MODEL_TOTAL_BYTES = "model_total_bytes"
        const val KEY_DOWNLOAD_RECEIVED_BYTES = "download_received_bytes"
    }

    override fun downloadModel(
        model: ModelInfo,
        onStatusUpdated: (DownloadProgress) -> Unit,
    ): UUID {
        val inputData = Data.Builder()
            .putString(KEY_MODEL_URL, model.downloadUrl)
            .putString(KEY_MODEL_NAME, model.id)
            .putString(KEY_MODEL_DOWNLOAD_FILE_NAME, model.downloadFileName)
            .putLong(KEY_MODEL_TOTAL_BYTES, model.downloadSizeBytes)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<DownloadWorker>()
            .setInputData(inputData)
            .addTag(model.id)
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)

        observeDownload(workRequest.id, model, onStatusUpdated)

        return workRequest.id
    }

    override fun observeDownload(
        workerId: UUID,
        model: ModelInfo,
        onStatusUpdated: (DownloadProgress) -> Unit,
    ) {
        WorkManager.getInstance(context)
            .getWorkInfoByIdLiveData(workerId)
            .observeForever { workInfo ->
                if (workInfo == null) return@observeForever

                val receivedBytes = workInfo.progress.getLong(KEY_DOWNLOAD_RECEIVED_BYTES, 0L)
                val totalBytes = workInfo.progress.getLong(KEY_MODEL_TOTAL_BYTES, model.downloadSizeBytes)

                val progress = DownloadProgress(
                    modelId = model.id,
                    totalBytes = totalBytes,
                    receivedBytes = receivedBytes,
                    isComplete = workInfo.state == WorkInfo.State.SUCCEEDED,
                    isFailed = workInfo.state == WorkInfo.State.FAILED,
                )
                onStatusUpdated(progress)
            }
    }

    override fun cancelDownload(model: ModelInfo) {
        WorkManager.getInstance(context).cancelAllWorkByTag(model.id)
    }

    override fun cancelAll(onComplete: () -> Unit) {
        WorkManager.getInstance(context).cancelAllWork()
        onComplete()
    }
}
