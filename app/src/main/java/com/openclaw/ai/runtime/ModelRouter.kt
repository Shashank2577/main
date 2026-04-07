package com.openclaw.ai.runtime

import com.openclaw.ai.data.model.ModelInfo
import com.openclaw.ai.data.model.ModelType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ModelRouter @Inject constructor(
    private val liteRtModelHelper: LiteRtModelHelper,
    private val geminiModelHelper: GeminiModelHelper,
) {

    /**
     * Returns the appropriate [LlmModelHelper] for the given model based on its type.
     */
    fun getHelper(model: ModelInfo): LlmModelHelper {
        return when (model.type) {
            ModelType.LOCAL -> liteRtModelHelper
            ModelType.CLOUD -> geminiModelHelper
        }
    }

    /**
     * Returns true when the request should be escalated to a cloud model.
     *
     * Escalation conditions:
     * - Model is LOCAL but the request includes images and the model does not support image input.
     * - Model is LOCAL but the request includes audio and the model does not support audio input.
     */
    fun shouldEscalateToCloud(
        model: ModelInfo,
        hasImages: Boolean,
        hasAudio: Boolean,
    ): Boolean {
        if (model.isCloud) return false
        if (hasImages && !model.supportsImage) return true
        if (hasAudio && !model.supportsAudio) return true
        return false
    }
}
