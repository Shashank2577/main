package com.openclaw.ai.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.openclaw.ai.data.repository.impl.DownloadRepositoryImpl.Companion.KEY_DOWNLOAD_RECEIVED_BYTES
import com.openclaw.ai.data.repository.impl.DownloadRepositoryImpl.Companion.KEY_MODEL_DOWNLOAD_FILE_NAME
import com.openclaw.ai.data.repository.impl.DownloadRepositoryImpl.Companion.KEY_MODEL_TOTAL_BYTES
import com.openclaw.ai.data.repository.impl.DownloadRepositoryImpl.Companion.KEY_MODEL_URL
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File

class DownloadWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val url = inputData.getString(KEY_MODEL_URL) ?: return Result.failure()
        val fileName = inputData.getString(KEY_MODEL_DOWNLOAD_FILE_NAME) ?: return Result.failure()
        val totalBytes = inputData.getLong(KEY_MODEL_TOTAL_BYTES, 0L)

        val destFile = File(applicationContext.getExternalFilesDir(null), fileName)

        return try {
            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()

            if (!response.isSuccessful) return Result.failure()

            val body = response.body ?: return Result.failure()
            var receivedBytes = 0L

            destFile.outputStream().use { output ->
                body.byteStream().use { input ->
                    val buffer = ByteArray(8 * 1024)
                    var read: Int
                    while (input.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                        receivedBytes += read
                        setProgress(
                            workDataOf(
                                KEY_DOWNLOAD_RECEIVED_BYTES to receivedBytes,
                                KEY_MODEL_TOTAL_BYTES to totalBytes,
                            )
                        )
                    }
                }
            }

            Result.success()
        } catch (e: Exception) {
            destFile.takeIf { it.exists() }?.delete()
            Result.failure()
        }
    }
}
