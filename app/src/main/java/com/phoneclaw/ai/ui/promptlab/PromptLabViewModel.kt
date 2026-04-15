package com.phoneclaw.ai.ui.promptlab

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phoneclaw.ai.data.Model
import com.phoneclaw.ai.data.repository.ModelRepository
import com.phoneclaw.ai.runtime.LlmModelHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class PromptLabViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val modelRepository: ModelRepository,
    private val llmModelHelper: LlmModelHelper,
) : ViewModel() {

    val currentModel: StateFlow<Model?> = modelRepository.activeModel

    private val _response = MutableStateFlow("")
    val response: StateFlow<String> = _response.asStateFlow()

    private val _isStreaming = MutableStateFlow(false)
    val isStreaming: StateFlow<Boolean> = _isStreaming.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var _modelInitialized = false
    private var _initializedModelId: String? = null

    fun runPrompt(prompt: String) {
        val model = currentModel.value ?: return
        if (prompt.isBlank()) return

        _isStreaming.value = true
        _response.value = ""
        _error.value = null

        val needsInit = !_modelInitialized || _initializedModelId != model.name
        if (needsInit) {
            llmModelHelper.initialize(
                context = context,
                model = model,
                supportImage = false,
                supportAudio = false,
                onDone = { error ->
                    if (error.isNotEmpty()) {
                        Log.e("PromptLabViewModel", "Init error: $error")
                        _error.value = "Could not load model. Make sure the model is fully downloaded."
                        _isStreaming.value = false
                        return@initialize
                    }
                    _modelInitialized = true
                    _initializedModelId = model.name
                    runSingleTurnInference(model, prompt)
                },
                tools = emptyList(),
                coroutineScope = viewModelScope,
            )
        } else {
            runSingleTurnInference(model, prompt)
        }
    }

    private fun runSingleTurnInference(model: Model, prompt: String) {
        // Reset conversation for single-turn isolation
        llmModelHelper.resetConversation(model)

        llmModelHelper.runInference(
            model = model,
            input = prompt,
            resultListener = { partial, done, _ ->
                _response.value += partial
                if (done) _isStreaming.value = false
            },
            cleanUpListener = {},
            onError = { err ->
                _error.value = err
                _isStreaming.value = false
            },
            coroutineScope = viewModelScope,
        )
    }

    fun clear() {
        _response.value = ""
        _error.value = null
    }

    fun stopGeneration() {
        currentModel.value?.let { llmModelHelper.stopResponse(it) }
        _isStreaming.value = false
    }
}
