package com.openclaw.ai.runtime

import android.content.Context
import android.graphics.Bitmap
import com.openclaw.ai.data.model.ModelInfo
import com.openclaw.ai.data.model.ToolDefinition
import kotlinx.coroutines.CoroutineScope

typealias ResultListener =
    (partialResult: String, done: Boolean, partialThinkingResult: String?) -> Unit

typealias CleanUpListener = () -> Unit

interface LlmModelHelper {

    fun initialize(
        context: Context,
        model: ModelInfo,
        supportImage: Boolean,
        supportAudio: Boolean,
        onDone: (String) -> Unit,
        systemInstruction: String? = null,
        tools: List<ToolDefinition> = emptyList(),
        coroutineScope: CoroutineScope? = null,
    )

    fun resetConversation(
        model: ModelInfo,
        supportImage: Boolean = false,
        supportAudio: Boolean = false,
        systemInstruction: String? = null,
        tools: List<ToolDefinition> = emptyList(),
    )

    fun cleanUp(model: ModelInfo, onDone: () -> Unit)

    fun runInference(
        model: ModelInfo,
        input: String,
        resultListener: ResultListener,
        cleanUpListener: CleanUpListener,
        onError: (message: String) -> Unit = {},
        images: List<Bitmap> = emptyList(),
        audioClips: List<ByteArray> = emptyList(),
        coroutineScope: CoroutineScope? = null,
        extraContext: Map<String, String>? = null,
    )

    fun stopResponse(model: ModelInfo)
}
