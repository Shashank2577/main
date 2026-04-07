package com.openclaw.ai.ui.chat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.openclaw.ai.ui.common.ModelPickerChip
import com.openclaw.ai.ui.theme.OpenClawAITheme

/**
 * The main chat screen — the primary screen users spend time on.
 *
 * @param viewModel              Injected [ChatViewModel].
 * @param onOpenDrawer           Called when the hamburger menu icon is tapped.
 * @param onOpenModelPicker      Called when the [ModelPickerChip] is tapped.
 * @param onOpenPerChatSettings  Called when per-chat settings should open.
 * @param onNavigateToVoice      Called when the user wants to switch to voice mode.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    onOpenDrawer: () -> Unit,
    onOpenModelPicker: () -> Unit,
    onOpenPerChatSettings: () -> Unit = {},
    onNavigateToVoice: () -> Unit = {},
    viewModel: ChatViewModel = hiltViewModel(),
) {
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val isStreaming by viewModel.isStreaming.collectAsStateWithLifecycle()
    val streamingText by viewModel.streamingText.collectAsStateWithLifecycle()
    val thinkingText by viewModel.thinkingText.collectAsStateWithLifecycle()
    val currentModel by viewModel.currentModel.collectAsStateWithLifecycle()
    val conversationTitle by viewModel.conversationTitle.collectAsStateWithLifecycle()

    var inputText by rememberSaveable { mutableStateOf("") }
    var showRenameDialog by rememberSaveable { mutableStateOf(false) }

    val listState = rememberLazyListState()
    val focusManager = LocalFocusManager.current
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    // Auto-scroll to the latest message when messages list changes or streaming progresses.
    LaunchedEffect(messages.size, streamingText) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            Column {
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = onOpenDrawer) {
                            Icon(
                                imageVector = Icons.Outlined.Menu,
                                contentDescription = "Open menu",
                            )
                        }
                    },
                    title = {
                        Text(
                            text = conversationTitle,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.clickable { showRenameDialog = true },
                        )
                    },
                    actions = {
                        ModelPickerChip(
                            modelName = currentModel?.displayName ?: "Select model",
                            onClick = onOpenModelPicker,
                            modifier = Modifier.padding(end = 8.dp),
                        )
                    },
                    scrollBehavior = scrollBehavior,
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        scrolledContainerColor = MaterialTheme.colorScheme.surface,
                    ),
                )

                // Quick action chips
                QuickActionChips(
                    onAction = { prompt ->
                        viewModel.sendQuickAction(prompt)
                        focusManager.clearFocus()
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        bottomBar = {
            InputBar(
                text = inputText,
                onTextChange = { inputText = it },
                onSend = { text ->
                    viewModel.sendMessage(text)
                    inputText = ""
                    focusManager.clearFocus()
                },
                onAttach = { /* File picker handled by caller */ },
                onVoiceToggle = onNavigateToVoice,
                onStop = viewModel::stopGeneration,
                isStreaming = isStreaming,
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding(),
        ) {
            if (messages.isEmpty() && !isStreaming) {
                // Empty state
                EmptyState(
                    onSuggestionClick = { prompt ->
                        viewModel.sendMessage(prompt)
                    },
                    modifier = Modifier.align(Alignment.Center),
                )
            } else {
                // Message list
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    items(
                        items = messages,
                        key = { it.id },
                    ) { message ->
                        val isThisMessageStreaming = isStreaming &&
                            message.isStreaming
                        MessageBubble(
                            message = message,
                            isStreaming = isThisMessageStreaming,
                            onImageClick = { /* image viewer handled at higher level */ },
                        )
                    }

                    // Thinking indicator shown while streaming and no tokens yet
                    if (isStreaming && streamingText.isEmpty()) {
                        item(key = "thinking_indicator") {
                            ThinkingIndicator(thinkingText = thinkingText)
                        }
                    }
                }
            }
        }
    }

    // Rename dialog
    if (showRenameDialog) {
        RenameDialog(
            currentTitle = conversationTitle,
            onConfirm = { newTitle ->
                viewModel.renameConversation(newTitle)
                showRenameDialog = false
            },
            onDismiss = { showRenameDialog = false },
        )
    }
}

// ---------------------------------------------------------------------------
// Empty state
// ---------------------------------------------------------------------------

private val SUGGESTIONS = listOf(
    "What can you help me with?",
    "Explain quantum computing simply",
    "Write a short poem about Kotlin",
    "Summarize the latest AI news",
)

@Composable
private fun EmptyState(
    onSuggestionClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Text(
            text = "How can I help you today?",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Text(
            text = "Ask me anything — I'm here to assist.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            SUGGESTIONS.forEach { suggestion ->
                TextButton(onClick = { onSuggestionClick(suggestion) }) {
                    Text(
                        text = suggestion,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Rename dialog
// ---------------------------------------------------------------------------

@Composable
private fun RenameDialog(
    currentTitle: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var title by rememberSaveable { mutableStateOf(currentTitle) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rename conversation") },
        text = {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                singleLine = true,
            )
        },
        confirmButton = {
            TextButton(
                onClick = { if (title.isNotBlank()) onConfirm(title.trim()) },
            ) {
                Text("Rename")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

// ---------------------------------------------------------------------------
// Preview
// ---------------------------------------------------------------------------

@Preview(showBackground = true, name = "ChatScreen - Empty State")
@Composable
private fun ChatScreenEmptyPreview() {
    OpenClawAITheme {
        EmptyState(onSuggestionClick = {})
    }
}
