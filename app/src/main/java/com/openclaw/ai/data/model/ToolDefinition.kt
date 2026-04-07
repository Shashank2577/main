package com.openclaw.ai.data.model

data class ToolParameter(
    val name: String,
    val type: String,
    val description: String,
    val required: Boolean = true,
)

data class ToolDefinition(
    val name: String,
    val description: String,
    val parameters: List<ToolParameter> = emptyList(),
)

data class ToolInvocation(
    val toolName: String,
    val parameters: Map<String, Any?>,
)

data class ToolResult(
    val toolName: String,
    val output: String,
    val isError: Boolean = false,
)
