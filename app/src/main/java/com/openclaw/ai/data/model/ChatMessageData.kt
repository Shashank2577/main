package com.openclaw.ai.data.model

enum class MessageRole(val value: String) {
    USER("user"),
    ASSISTANT("assistant"),
    TOOL("tool"),
    SYSTEM("system");

    companion object {
        fun fromValue(value: String): MessageRole =
            entries.firstOrNull { it.value == value } ?: USER
    }
}

data class ChatMessageData(
    val id: String,
    val conversationId: String,
    val role: MessageRole,
    val content: String,
    val mediaUri: String? = null,
    val toolName: String? = null,
    val toolParams: String? = null,
    val toolResult: String? = null,
    val timestamp: Long,
    val tokens: Int? = null,
    val isStreaming: Boolean = false,
)

data class ConversationWithLastMessage(
    val id: String,
    val spaceId: String,
    val title: String,
    val lastMessageAt: Long,
    val lastMessagePreview: String? = null,
)
