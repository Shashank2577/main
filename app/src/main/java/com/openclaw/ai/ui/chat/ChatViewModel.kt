package com.openclaw.ai.ui.chat

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openclaw.ai.data.db.entity.MessageEntity
import com.openclaw.ai.data.model.ChatMessageData
import com.openclaw.ai.data.model.MessageRole
import com.openclaw.ai.data.model.ModelInfo
import com.openclaw.ai.data.repository.ConversationRepository
import com.openclaw.ai.data.repository.ModelRepository
import com.openclaw.ai.data.repository.SettingsRepository
import com.openclaw.ai.runtime.InferenceConfig
import com.openclaw.ai.runtime.ModelRouter
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val conversationRepository: ConversationRepository,
    private val settingsRepository: SettingsRepository,
    private val modelRepository: ModelRepository,
    private val modelRouter: ModelRouter,
) : ViewModel() {

    // -------------------------------------------------------------------------
    // State
    // -------------------------------------------------------------------------

    private val _currentConversationId = MutableStateFlow<String?>(null)
    val currentConversationId: StateFlow<String?> = _currentConversationId.asStateFlow()

    private val _conversationTitle = MutableStateFlow("New Chat")
    val conversationTitle: StateFlow<String> = _conversationTitle.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessageData>>(emptyList())
    val messages: StateFlow<List<ChatMessageData>> = _messages.asStateFlow()

    private val _isStreaming = MutableStateFlow(false)
    val isStreaming: StateFlow<Boolean> = _isStreaming.asStateFlow()

    /** Accumulates tokens as they arrive during streaming. */
    private val _streamingText = MutableStateFlow("")
    val streamingText: StateFlow<String> = _streamingText.asStateFlow()

    /** Thinking / chain-of-thought text emitted by the model during streaming. */
    private val _thinkingText = MutableStateFlow("")
    val thinkingText: StateFlow<String> = _thinkingText.asStateFlow()

    val currentModel: StateFlow<ModelInfo?> = modelRepository.activeModel

    /** Active inference job — kept so it can be cancelled by [stopGeneration]. */
    private var inferenceJob: Job? = null

    /** ID of the placeholder assistant message appended at stream start. */
    private var streamingMessageId: String? = null

    // -------------------------------------------------------------------------
    // Initialisation
    // -------------------------------------------------------------------------

    init {
        // Observe the active model and initialise the helper whenever it changes.
        viewModelScope.launch {
            modelRepository.activeModel.collectLatest { model ->
                if (model != null) {
                    val helper = modelRouter.getHelper(model)
                    helper.initialize(
                        context = context,
                        model = model,
                        supportImage = model.supportsImage,
                        supportAudio = model.supportsAudio,
                        onDone = { /* handled per-inference */ },
                        coroutineScope = viewModelScope,
                    )
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Conversation management
    // -------------------------------------------------------------------------

    fun loadConversation(conversationId: String) {
        viewModelScope.launch {
            val conversation = conversationRepository.getConversation(conversationId) ?: return@launch
            _currentConversationId.value = conversationId
            _conversationTitle.value = conversation.title

            conversationRepository.getMessages(conversationId).collect { entities ->
                _messages.value = entities.map { it.toChatMessageData() }
            }
        }
    }

    fun createNewConversation(spaceId: String) {
        viewModelScope.launch {
            val conversation = conversationRepository.createConversation(spaceId = spaceId)
            _currentConversationId.value = conversation.id
            _conversationTitle.value = conversation.title
            _messages.value = emptyList()
        }
    }

    fun renameConversation(title: String) {
        val conversationId = _currentConversationId.value ?: return
        viewModelScope.launch {
            conversationRepository.updateTitle(conversationId, title)
            _conversationTitle.value = title
        }
    }

    // -------------------------------------------------------------------------
    // Sending messages
    // -------------------------------------------------------------------------

    fun sendMessage(text: String, images: List<Bitmap> = emptyList()) {
        val conversationId = _currentConversationId.value ?: return
        val model = modelRepository.activeModel.value ?: return
        if (text.isBlank()) return

        inferenceJob = viewModelScope.launch {
            // 1. Persist the user message.
            val userEntity = MessageEntity(
                id = UUID.randomUUID().toString(),
                conversationId = conversationId,
                role = MessageRole.USER.value,
                content = text,
                timestamp = System.currentTimeMillis(),
            )
            conversationRepository.addMessage(userEntity)

            // 2. Create a placeholder streaming assistant message in-memory.
            val assistantId = UUID.randomUUID().toString()
            streamingMessageId = assistantId
            _streamingText.value = ""
            _thinkingText.value = ""
            _isStreaming.value = true

            val placeholderMessage = ChatMessageData(
                id = assistantId,
                conversationId = conversationId,
                role = MessageRole.ASSISTANT,
                content = "",
                timestamp = System.currentTimeMillis(),
                isStreaming = true,
            )
            _messages.update { it + placeholderMessage }

            // 3. Resolve inference config.
            val perChatSettings = settingsRepository.getPerChatSettings(conversationId)
            val config = perChatSettings
                ?.let { InferenceConfig.fromPerChatSettings(it) }
                ?: InferenceConfig.DEFAULT

            // 4. Run inference.
            val helper = modelRouter.getHelper(model)
            helper.runInference(
                model = model,
                input = text,
                images = images,
                resultListener = { partial, done, partialThinking ->
                    _streamingText.update { it + partial }
                    if (partialThinking != null) {
                        _thinkingText.update { it + partialThinking }
                    }

                    // Keep the in-memory streaming bubble updated.
                    _messages.update { list ->
                        list.map { msg ->
                            if (msg.id == assistantId) {
                                msg.copy(
                                    content = _streamingText.value,
                                    isStreaming = !done,
                                )
                            } else msg
                        }
                    }

                    if (done) {
                        finishStreaming(
                            conversationId = conversationId,
                            assistantId = assistantId,
                            fullText = _streamingText.value,
                        )
                    }
                },
                cleanUpListener = {
                    // Ensure streaming state is cleared even on unexpected cleanup.
                    if (_isStreaming.value) {
                        finishStreaming(
                            conversationId = conversationId,
                            assistantId = assistantId,
                            fullText = _streamingText.value,
                        )
                    }
                },
                onError = { errorMessage ->
                    _isStreaming.value = false
                    _messages.update { list ->
                        list.map { msg ->
                            if (msg.id == assistantId) {
                                msg.copy(
                                    content = "Error: $errorMessage",
                                    isStreaming = false,
                                )
                            } else msg
                        }
                    }
                },
                coroutineScope = viewModelScope,
                extraContext = config.systemPrompt?.let { mapOf("systemPrompt" to it) },
            )
        }
    }

    fun sendQuickAction(prompt: String) {
        sendMessage(prompt)
    }

    fun stopGeneration() {
        val model = modelRepository.activeModel.value ?: return
        modelRouter.getHelper(model).stopResponse(model)
        inferenceJob?.cancel()
        _isStreaming.value = false
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private fun finishStreaming(
        conversationId: String,
        assistantId: String,
        fullText: String,
    ) {
        _isStreaming.value = false
        streamingMessageId = null

        // Persist the completed assistant message.
        viewModelScope.launch {
            val assistantEntity = MessageEntity(
                id = assistantId,
                conversationId = conversationId,
                role = MessageRole.ASSISTANT.value,
                content = fullText,
                timestamp = System.currentTimeMillis(),
            )
            conversationRepository.addMessage(assistantEntity)
        }
    }

    // -------------------------------------------------------------------------
    // Cleanup
    // -------------------------------------------------------------------------

    override fun onCleared() {
        super.onCleared()
        val model = modelRepository.activeModel.value
        if (model != null) {
            modelRouter.getHelper(model).cleanUp(model, onDone = {})
        }
    }
}

// ---------------------------------------------------------------------------
// Extension: MessageEntity -> ChatMessageData
// ---------------------------------------------------------------------------

private fun MessageEntity.toChatMessageData(): ChatMessageData = ChatMessageData(
    id = id,
    conversationId = conversationId,
    role = MessageRole.fromValue(role),
    content = content,
    mediaUri = mediaUri,
    toolName = toolName,
    toolParams = toolParams,
    toolResult = toolResult,
    timestamp = timestamp,
    tokens = tokens,
)
