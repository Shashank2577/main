package com.openclaw.ai.tools.builtin

import com.openclaw.ai.data.model.ToolDefinition
import com.openclaw.ai.data.model.ToolInvocation
import com.openclaw.ai.data.model.ToolParameter
import com.openclaw.ai.data.model.ToolResult
import com.openclaw.ai.tools.ToolExecutor
import java.time.Instant
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

private data class Task(
    val id: Int,
    val title: String,
    val completed: Boolean = false,
    val createdAt: Instant = Instant.now(),
)

@Singleton
class TaskManagerTool @Inject constructor() : ToolExecutor {

    private val tasks: MutableList<Task> = mutableListOf()
    private val nextId = AtomicInteger(1)

    override val definition = ToolDefinition(
        name = "task_manager",
        description = "Manage a simple task list. Supports adding, listing, completing, and deleting tasks.",
        parameters = listOf(
            ToolParameter(
                name = "action",
                type = "string",
                description = "Action to perform: 'add', 'list', 'complete', or 'delete'.",
                required = true,
            ),
            ToolParameter(
                name = "title",
                type = "string",
                description = "Title of the task (required for 'add' action).",
                required = false,
            ),
            ToolParameter(
                name = "taskId",
                type = "string",
                description = "ID of the task (required for 'complete' and 'delete' actions).",
                required = false,
            ),
        ),
    )

    override suspend fun execute(invocation: ToolInvocation): ToolResult {
        val action = invocation.parameters["action"]?.toString()?.trim()?.lowercase()
            ?: return ToolResult(
                toolName = invocation.toolName,
                output = "Missing required parameter: 'action'. Use 'add', 'list', 'complete', or 'delete'.",
                isError = true,
            )

        return when (action) {
            "add" -> handleAdd(invocation)
            "list" -> handleList(invocation)
            "complete" -> handleComplete(invocation)
            "delete" -> handleDelete(invocation)
            else -> ToolResult(
                toolName = invocation.toolName,
                output = "Unknown action '$action'. Valid actions: 'add', 'list', 'complete', 'delete'.",
                isError = true,
            )
        }
    }

    private fun handleAdd(invocation: ToolInvocation): ToolResult {
        val title = invocation.parameters["title"]?.toString()?.trim()
        if (title.isNullOrBlank()) {
            return ToolResult(
                toolName = invocation.toolName,
                output = "Missing required parameter: 'title' for action 'add'.",
                isError = true,
            )
        }
        val task = Task(id = nextId.getAndIncrement(), title = title)
        tasks.add(task)
        return ToolResult(
            toolName = invocation.toolName,
            output = "Task added successfully.\n\n**Task #${task.id}**: ${task.title}",
        )
    }

    private fun handleList(invocation: ToolInvocation): ToolResult {
        if (tasks.isEmpty()) {
            return ToolResult(
                toolName = invocation.toolName,
                output = "No tasks found. Use the 'add' action to create a task.",
            )
        }
        val sb = StringBuilder("**Your Tasks**\n\n")
        tasks.forEach { task ->
            val status = if (task.completed) "[x]" else "[ ]"
            sb.appendLine("$status **#${task.id}** ${task.title}")
        }
        val total = tasks.size
        val done = tasks.count { it.completed }
        sb.append("\n_$done of $total tasks completed._")
        return ToolResult(toolName = invocation.toolName, output = sb.toString())
    }

    private fun handleComplete(invocation: ToolInvocation): ToolResult {
        val taskId = resolveTaskId(invocation)
            ?: return ToolResult(
                toolName = invocation.toolName,
                output = "Missing required parameter: 'taskId' for action 'complete'.",
                isError = true,
            )
        val index = tasks.indexOfFirst { it.id == taskId }
        if (index == -1) {
            return ToolResult(
                toolName = invocation.toolName,
                output = "Task #$taskId not found.",
                isError = true,
            )
        }
        tasks[index] = tasks[index].copy(completed = true)
        return ToolResult(
            toolName = invocation.toolName,
            output = "Task #$taskId marked as completed: **${tasks[index].title}**",
        )
    }

    private fun handleDelete(invocation: ToolInvocation): ToolResult {
        val taskId = resolveTaskId(invocation)
            ?: return ToolResult(
                toolName = invocation.toolName,
                output = "Missing required parameter: 'taskId' for action 'delete'.",
                isError = true,
            )
        val task = tasks.find { it.id == taskId }
            ?: return ToolResult(
                toolName = invocation.toolName,
                output = "Task #$taskId not found.",
                isError = true,
            )
        tasks.remove(task)
        return ToolResult(
            toolName = invocation.toolName,
            output = "Task #$taskId deleted: **${task.title}**",
        )
    }

    private fun resolveTaskId(invocation: ToolInvocation): Int? {
        return invocation.parameters["taskId"]?.toString()?.trim()?.toIntOrNull()
    }
}
