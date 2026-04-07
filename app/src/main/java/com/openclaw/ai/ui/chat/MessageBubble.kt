package com.openclaw.ai.ui.chat

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BrokenImage
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.openclaw.ai.data.model.ChatMessageData
import com.openclaw.ai.data.model.MessageRole
import com.openclaw.ai.ui.common.MarkdownText
import com.openclaw.ai.ui.theme.OpenClawAITheme

/**
 * A message bubble representing either a user message or an assistant message.
 *
 * @param message       The [ChatMessageData] containing the message content.
 * @param isStreaming   Whether this message is currently being streamed.
 * @param onImageClick  Called when an attached image is tapped.
 */
@Composable
fun MessageBubble(
    message: ChatMessageData,
    isStreaming: Boolean = false,
    onImageClick: (String) -> Unit = {},
) {
    val isUser = message.role == MessageRole.USER

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart,
    ) {
        if (isUser) {
            UserBubble(message, onImageClick)
        } else {
            AssistantBubble(message, isStreaming)
        }
    }
}

@Composable
private fun UserBubble(
    message: ChatMessageData,
    onImageClick: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 56.dp, end = 12.dp, top = 4.dp, bottom = 4.dp),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        // Image attachment thumbnail
        if (!message.mediaUri.isNullOrBlank()) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(message.mediaUri)
                    .crossfade(true)
                    .build(),
                contentDescription = "Attached image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(160.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onImageClick(message.mediaUri!!) },
                error = {
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .background(
                                MaterialTheme.colorScheme.surfaceContainerHigh,
                                RoundedCornerShape(12.dp),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.BrokenImage,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
            )
        }

        // Voice transcript badge
        if (message.content.isNotBlank() && message.mediaUri != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                        RoundedCornerShape(16.dp),
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Mic,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Transcript",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
        }

        // Text bubble
        if (message.content.isNotBlank()) {
            Card(
                shape = RoundedCornerShape(16.dp, 4.dp, 16.dp, 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.widthIn(max = 300.dp),
            ) {
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                )
            }
        }
    }
}

@Composable
private fun AssistantBubble(
    message: ChatMessageData,
    isStreaming: Boolean,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 48.dp, top = 4.dp, bottom = 4.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        // Thinking indicator (optional, if the model supports it)
        // Here we just render the markdown content directly
        MarkdownText(
            text = message.content,
            modifier = Modifier.fillMaxWidth(),
        )

        if (isStreaming && message.content.isEmpty()) {
            StreamingIndicator()
        }
    }
}

@Composable
private fun StreamingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "streaming")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "alpha",
    )

    Box(
        modifier = Modifier
            .size(8.dp)
            .background(
                MaterialTheme.colorScheme.primary.copy(alpha = alpha),
                RoundedCornerShape(4.dp),
            ),
    )
}

@Preview(showBackground = true, name = "User Message")
@Composable
private fun UserMessagePreview() {
    OpenClawAITheme {
        MessageBubble(
            message = ChatMessageData(
                id = "1",
                conversationId = "conv-1",
                role = MessageRole.USER,
                content = "Hello! How can I use Kotlin coroutines for network calls?",
                timestamp = System.currentTimeMillis(),
            ),
        )
    }
}

@Preview(showBackground = true, name = "Assistant Message")
@Composable
private fun AssistantMessagePreview() {
    OpenClawAITheme {
        MessageBubble(
            message = ChatMessageData(
                id = "2",
                conversationId = "conv-1",
                role = MessageRole.ASSISTANT,
                content = "Kotlin coroutines are a feature of the Kotlin language that allows for asynchronous programming...",
                timestamp = System.currentTimeMillis(),
            ),
        )
    }
}
