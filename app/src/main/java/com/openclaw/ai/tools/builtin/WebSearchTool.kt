package com.openclaw.ai.tools.builtin

import com.openclaw.ai.data.model.ToolDefinition
import com.openclaw.ai.data.model.ToolInvocation
import com.openclaw.ai.data.model.ToolParameter
import com.openclaw.ai.data.model.ToolResult
import com.openclaw.ai.tools.ToolExecutor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebSearchTool @Inject constructor() : ToolExecutor {

    override val definition = ToolDefinition(
        name = "web_search",
        description = "Search the web for information. Only available when a cloud model is active.",
        parameters = listOf(
            ToolParameter(
                name = "query",
                type = "string",
                description = "The search query.",
                required = true,
            ),
        ),
    )

    override suspend fun execute(invocation: ToolInvocation): ToolResult {
        val query = invocation.parameters["query"]?.toString()?.trim()
        if (query.isNullOrBlank()) {
            return ToolResult(
                toolName = invocation.toolName,
                output = "Missing required parameter: 'query'.",
                isError = true,
            )
        }

        // Placeholder: web search requires a cloud search API not yet integrated.
        return ToolResult(
            toolName = invocation.toolName,
            output = "Web search is not yet available. Please check back later.",
        )
    }
}
