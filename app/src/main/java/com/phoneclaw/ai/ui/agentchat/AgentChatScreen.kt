package com.phoneclaw.ai.ui.agentchat

import androidx.compose.runtime.Composable
import com.phoneclaw.ai.ui.chat.ChatScreen

/**
 * Agent Chat screen — wraps ChatScreen with agent mode enabled.
 * Agent mode causes the ChatViewModel to always pass tools and
 * enable constrained decoding, regardless of selected skills.
 */
@Composable
fun AgentChatScreen(
    onOpenDrawer: () -> Unit,
    onOpenModelPicker: () -> Unit,
    onOpenPerChatSettings: (String) -> Unit = {},
    onNavigateToVoice: () -> Unit = {},
    agentMode: Boolean = true,
) {
    ChatScreen(
        onOpenDrawer = onOpenDrawer,
        onOpenModelPicker = onOpenModelPicker,
        onOpenPerChatSettings = onOpenPerChatSettings,
        onNavigateToVoice = onNavigateToVoice,
        agentMode = agentMode,
    )
}
