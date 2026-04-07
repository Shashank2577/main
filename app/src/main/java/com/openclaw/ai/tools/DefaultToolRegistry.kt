package com.openclaw.ai.tools

import com.openclaw.ai.data.model.ToolDefinition
import com.openclaw.ai.data.model.ToolInvocation
import com.openclaw.ai.data.model.ToolResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultToolRegistry @Inject constructor() : ToolRegistry {

    private val executors: MutableMap<String, ToolExecutor> = mutableMapOf()

    override fun register(executor: ToolExecutor) {
        executors[executor.definition.name] = executor
    }

    override fun unregister(toolName: String) {
        executors.remove(toolName)
    }

    override fun getAvailableTools(): List<ToolDefinition> {
        return executors.values.map { it.definition }
    }

    override fun getEnabledTools(toolNames: List<String>?): List<ToolDefinition> {
        if (toolNames == null) return getAvailableTools()
        return executors.values
            .filter { it.definition.name in toolNames }
            .map { it.definition }
    }

    override suspend fun executeTool(invocation: ToolInvocation): ToolResult {
        val executor = executors[invocation.toolName]
            ?: return ToolResult(
                toolName = invocation.toolName,
                output = "Tool '${invocation.toolName}' is not registered.",
                isError = true,
            )
        return try {
            executor.execute(invocation)
        } catch (e: Exception) {
            ToolResult(
                toolName = invocation.toolName,
                output = "Tool execution failed: ${e.message ?: "Unknown error"}",
                isError = true,
            )
        }
    }

    override fun isToolAvailable(toolName: String): Boolean {
        return executors.containsKey(toolName)
    }
}
