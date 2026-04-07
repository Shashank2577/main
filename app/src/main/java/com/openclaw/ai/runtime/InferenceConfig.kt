package com.openclaw.ai.runtime

import com.openclaw.ai.data.db.entity.PerChatSettingsEntity
import com.openclaw.ai.data.model.ToolDefinition

data class InferenceConfig(
    val temperature: Float,
    val topK: Int,
    val topP: Float,
    val maxTokens: Int,
    val systemPrompt: String?,
    val tools: List<ToolDefinition>,
) {
    companion object {
        val DEFAULT = InferenceConfig(
            temperature = 0.7f,
            topK = 40,
            topP = 0.95f,
            maxTokens = 4096,
            systemPrompt = null,
            tools = emptyList(),
        )

        fun fromPerChatSettings(settings: PerChatSettingsEntity): InferenceConfig {
            return InferenceConfig(
                temperature = settings.temperature,
                topK = settings.topK,
                topP = settings.topP,
                maxTokens = settings.maxTokens,
                systemPrompt = null,
                tools = emptyList(),
            )
        }
    }
}
