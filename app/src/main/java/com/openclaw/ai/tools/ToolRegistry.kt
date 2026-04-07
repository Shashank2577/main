package com.openclaw.ai.tools

import com.openclaw.ai.data.model.ToolDefinition
import com.openclaw.ai.data.model.ToolInvocation
import com.openclaw.ai.data.model.ToolResult

interface ToolExecutor {
    val definition: ToolDefinition
    suspend fun execute(invocation: ToolInvocation): ToolResult
}

interface ToolRegistry {

    fun getAvailableTools(): List<ToolDefinition>

    fun getEnabledTools(toolNames: List<String>? = null): List<ToolDefinition>

    fun register(executor: ToolExecutor)

    fun unregister(toolName: String)

    suspend fun executeTool(invocation: ToolInvocation): ToolResult

    fun isToolAvailable(toolName: String): Boolean
}
