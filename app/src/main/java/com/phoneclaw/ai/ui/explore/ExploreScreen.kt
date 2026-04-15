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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
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
                        shadowElevation = 3.dp,
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
                            fontSize = 20.sp
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
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Section header
            Text(
                text = "What would you like to do?",
                style = MaterialTheme.typography.bodyMedium,
                color = ForegroundSecondary,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
            )

            TaskCard(
                icon = Icons.Rounded.AutoAwesome,
                iconColor = AccentViolet,
                cardColor = SurfaceVioletCard,
                shadowColor = ShadowVioletCard,
                title = "AI Chat",
                description = "Ask anything, get intelligent answers from a local AI model",
                onClick = { onNavigateToChatWithMode(ChatMode.CHAT) },
            )
            TaskCard(
                icon = Icons.Outlined.Psychology,
                iconColor = AccentPink,
                cardColor = SurfacePinkCard,
                shadowColor = ShadowPinkCard,
                title = "Agent Skills",
                description = "Complete tasks with AI-powered skills: maps, web, email, and more",
                onClick = { onNavigateToChatWithMode(ChatMode.AGENT) },
            )
            TaskCard(
                icon = Icons.Outlined.Image,
                iconColor = AccentBlue,
                cardColor = SurfaceBlueCard,
                shadowColor = ShadowBlueCard,
                title = "Ask Image",
                description = "Attach a photo and ask questions about what you see",
                onClick = { onNavigateToChatWithMode(ChatMode.ASK_IMAGE) },
            )
            TaskCard(
                icon = Icons.Outlined.Science,
                iconColor = AccentGreen,
                cardColor = SurfaceGreenCard,
                shadowColor = ShadowGreenCard,
                title = "Prompt Lab",
                description = "Experiment with templates: rewrite tone, summarize, generate code",
                onClick = { onNavigateToChatWithMode(ChatMode.PROMPT_LAB) },
            )
            TaskCard(
                icon = Icons.Outlined.Mic,
                iconColor = AccentAmber,
                cardColor = SurfaceAmberCard,
                shadowColor = ShadowAmberCard,
                title = "Voice",
                description = "Speak naturally with your AI assistant",
                onClick = onNavigateToVoice,
            )
        }
    }
}

@Composable
private fun TaskCard(
    icon: ImageVector,
    iconColor: Color,
    cardColor: Color,
    shadowColor: Color,
    title: String,
    description: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = shadowColor,
                spotColor = shadowColor,
            )
    ) {
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(24.dp),
            color = cardColor,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(iconColor.copy(alpha = 0.18f), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(26.dp),
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
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                        color = ForegroundSecondary,
                    )
                }
            }
        }
    }
}
