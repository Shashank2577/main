package com.openclaw.ai.tools.builtin

import android.content.Context
import com.openclaw.ai.data.model.ToolDefinition
import com.openclaw.ai.data.model.ToolInvocation
import com.openclaw.ai.data.model.ToolParameter
import com.openclaw.ai.data.model.ToolResult
import com.openclaw.ai.tools.ToolExecutor
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileSearchTool @Inject constructor(
    @ApplicationContext private val context: Context,
) : ToolExecutor {

    override val definition = ToolDefinition(
        name = "file_search",
        description = "Search files in the current knowledge space by name.",
        parameters = listOf(
            ToolParameter(
                name = "query",
                type = "string",
                description = "Search term to match against file names.",
                required = true,
            ),
            ToolParameter(
                name = "spaceId",
                type = "string",
                description = "ID of the knowledge space to search within.",
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

        val spaceId = invocation.parameters["spaceId"]?.toString()?.trim()
        if (spaceId.isNullOrBlank()) {
            return ToolResult(
                toolName = invocation.toolName,
                output = "Missing required parameter: 'spaceId'.",
                isError = true,
            )
        }

        val spaceDir = File(context.filesDir, "spaces/$spaceId")
        if (!spaceDir.exists() || !spaceDir.isDirectory) {
            return ToolResult(
                toolName = invocation.toolName,
                output = "Space '$spaceId' not found or has no files.",
            )
        }

        val matches = spaceDir.walkTopDown()
            .filter { it.isFile && it.name.contains(query, ignoreCase = true) }
            .toList()

        if (matches.isEmpty()) {
            return ToolResult(
                toolName = invocation.toolName,
                output = "No files matching **\"$query\"** found in space `$spaceId`.",
            )
        }

        val sb = StringBuilder()
        sb.appendLine("## File Search Results")
        sb.appendLine()
        sb.appendLine("Query: **\"$query\"** in space `$spaceId`")
        sb.appendLine("Found **${matches.size}** file(s):")
        sb.appendLine()

        matches.forEach { file ->
            val relativePath = file.relativeTo(spaceDir).path
            val sizeLabel = formatFileSize(file.length())
            sb.appendLine("- `$relativePath` ($sizeLabel)")
        }

        return ToolResult(toolName = invocation.toolName, output = sb.toString().trimEnd())
    }

    private fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024L -> "${bytes} B"
            bytes < 1024L * 1024L -> "${"%.1f".format(bytes / 1024.0)} KB"
            else -> "${"%.1f".format(bytes / (1024.0 * 1024.0))} MB"
        }
    }
}
