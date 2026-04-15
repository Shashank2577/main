package com.phoneclaw.ai.ui.explore

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.phoneclaw.ai.ui.chat.ChatMode
import com.phoneclaw.ai.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    onNavigateToChatWithMode: (ChatMode) -> Unit,
    onNavigateToVoice: () -> Unit,
    onOpenDrawer: () -> Unit,
) {
    Scaffold(
        containerColor = CanvasBg,
        topBar = {
            Column(modifier = Modifier.background(CanvasBg)) {
                Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        onClick = onOpenDrawer,
                        shape = RoundedCornerShape(12.dp),
                        color = SurfaceCard,
                        shadowElevation = 2.dp,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.Menu,
                                contentDescription = "Menu",
                                tint = ForegroundPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Explore",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = NunitoFontFamily,
                            fontSize = 18.sp
                        ),
                        color = ForegroundPrimary,
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            TaskCard(
                icon = Icons.Rounded.AutoAwesome,
                iconColor = AccentViolet,
                title = "AI Chat",
                description = "Chat with a local AI model",
                onClick = { onNavigateToChatWithMode(ChatMode.CHAT) },
            )
            TaskCard(
                icon = Icons.Outlined.Psychology,
                iconColor = AccentPink,
                title = "Agent Skills",
                description = "Complete tasks using AI agents",
                onClick = { onNavigateToChatWithMode(ChatMode.AGENT) },
            )
            TaskCard(
                icon = Icons.Outlined.Image,
                iconColor = AccentBlue,
                title = "Ask Image",
                description = "Ask questions about images",
                onClick = { onNavigateToChatWithMode(ChatMode.ASK_IMAGE) },
            )
            TaskCard(
                icon = Icons.Outlined.Science,
                iconColor = AccentGreen,
                title = "Prompt Lab",
                description = "Single-turn prompt testing",
                onClick = { onNavigateToChatWithMode(ChatMode.PROMPT_LAB) },
            )
            TaskCard(
                icon = Icons.Outlined.Mic,
                iconColor = AccentAmber,
                title = "Voice",
                description = "Talk with your AI assistant",
                onClick = onNavigateToVoice,
            )
        }
    }
}

@Composable
private fun TaskCard(
    icon: ImageVector,
    iconColor: androidx.compose.ui.graphics.Color,
    title: String,
    description: String,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = SurfaceCard,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(iconColor.copy(alpha = 0.15f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                    ),
                    color = ForegroundPrimary,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = ForegroundSecondary,
                )
            }
        }
    }
}
