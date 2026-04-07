package com.openclaw.ai.data.model

import kotlinx.serialization.Serializable

enum class ModelType {
    LOCAL,
    CLOUD,
}

enum class ModelDownloadStatus {
    NOT_DOWNLOADED,
    DOWNLOADING,
    DOWNLOADED,
    FAILED,
}

@Serializable
data class ModelInfo(
    val id: String,
    val name: String,
    val displayName: String = name,
    val type: ModelType,
    val sizeLabel: String = "",         // e.g., "2B", "7B"
    val downloadSizeBytes: Long = 0L,
    val downloadUrl: String = "",
    val downloadFileName: String = "",
    val version: String = "1",
    val supportsImage: Boolean = false,
    val supportsAudio: Boolean = false,
    val supportsThinking: Boolean = false,
    val supportsTools: Boolean = false,
    val maxContextLength: Int = 8192,
    val defaultMaxTokens: Int = 4096,
    val defaultTemperature: Float = 0.7f,
    val defaultTopK: Int = 40,
    val defaultTopP: Float = 0.95f,
) {
    val isLocal: Boolean get() = type == ModelType.LOCAL
    val isCloud: Boolean get() = type == ModelType.CLOUD
}

object DefaultModels {
    val GEMMA_4 = ModelInfo(
        id = "gemma-4-2b",
        name = "Gemma 4",
        displayName = "Gemma 4",
        type = ModelType.LOCAL,
        sizeLabel = "2B",
        downloadSizeBytes = 1_800_000_000L, // ~1.8 GB
        downloadUrl = "", // Set from config
        downloadFileName = "gemma-4-2b.bin",
        supportsImage = false,
        supportsThinking = true,
        maxContextLength = 8192,
    )

    val GEMINI_FLASH = ModelInfo(
        id = "gemini-2.0-flash",
        name = "Gemini 2.0 Flash",
        displayName = "Gemini Flash",
        type = ModelType.CLOUD,
        sizeLabel = "",
        supportsImage = true,
        supportsAudio = true,
        supportsTools = true,
        maxContextLength = 1_000_000,
        defaultMaxTokens = 8192,
    )

    val GEMINI_PRO = ModelInfo(
        id = "gemini-2.0-pro",
        name = "Gemini 2.0 Pro",
        displayName = "Gemini Pro",
        type = ModelType.CLOUD,
        sizeLabel = "",
        supportsImage = true,
        supportsAudio = true,
        supportsTools = true,
        maxContextLength = 2_000_000,
        defaultMaxTokens = 8192,
    )

    val ALL = listOf(GEMMA_4, GEMINI_FLASH, GEMINI_PRO)
    val LOCAL_MODELS = ALL.filter { it.isLocal }
    val CLOUD_MODELS = ALL.filter { it.isCloud }
}
