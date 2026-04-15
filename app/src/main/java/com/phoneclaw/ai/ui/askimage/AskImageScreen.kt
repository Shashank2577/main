package com.phoneclaw.ai.ui.askimage

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.phoneclaw.ai.ui.common.MarkdownText
import com.phoneclaw.ai.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AskImageScreen(
    onBack: () -> Unit,
    onOpenModelPicker: () -> Unit,
    viewModel: AskImageViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val response by viewModel.response.collectAsStateWithLifecycle()
    val isStreaming by viewModel.isStreaming.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val currentModel by viewModel.currentModel.collectAsStateWithLifecycle()

    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var selectedImageUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var questionText by remember { mutableStateOf("") }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            selectedImageUri = it
            context.contentResolver.openInputStream(it)?.use { stream ->
                selectedBitmap = BitmapFactory.decodeStream(stream)
            }
        }
    }

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
                        onClick = onBack,
                        shape = RoundedCornerShape(12.dp),
                        color = SurfaceCard,
                        shadowElevation = 2.dp,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.ArrowBack,
                                contentDescription = "Back",
                                tint = ForegroundPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Ask Image",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = NunitoFontFamily,
                            fontSize = 18.sp
                        ),
                        color = ForegroundPrimary,
                        modifier = Modifier.weight(1f),
                    )
                    Surface(
                        onClick = onOpenModelPicker,
                        shape = RoundedCornerShape(24.dp),
                        color = AccentViolet,
                        modifier = Modifier.height(32.dp),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AutoAwesome,
                                contentDescription = null,
                                tint = ForegroundInverse,
                                modifier = Modifier.size(14.dp),
                            )
                            Text(
                                text = currentModel?.displayName ?: "Model",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                ),
                                color = ForegroundInverse,
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Image picker area
            Surface(
                onClick = { imagePicker.launch("image/*") },
                shape = RoundedCornerShape(20.dp),
                color = SurfaceCard,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 200.dp)
                    .border(
                        width = 2.dp,
                        color = ForegroundMuted.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(20.dp),
                    ),
            ) {
                if (selectedImageUri != null) {
                    AsyncImage(
                        model = selectedImageUri,
                        contentDescription = "Selected image",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 200.dp, max = 300.dp)
                            .clip(RoundedCornerShape(20.dp)),
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Image,
                            contentDescription = null,
                            tint = ForegroundMuted,
                            modifier = Modifier.size(48.dp),
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tap to pick an image",
                            style = MaterialTheme.typography.bodyLarge,
                            color = ForegroundSecondary,
                        )
                    }
                }
            }

            // Question text field
            OutlinedTextField(
                value = questionText,
                onValueChange = { questionText = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        "What do you want to know about this image?",
                        color = ForegroundMuted,
                    )
                },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentViolet,
                    unfocusedBorderColor = ForegroundMuted.copy(alpha = 0.3f),
                    focusedContainerColor = SurfaceCard,
                    unfocusedContainerColor = SurfaceCard,
                ),
                maxLines = 3,
            )

            // Ask button
            Button(
                onClick = {
                    selectedBitmap?.let { bitmap ->
                        viewModel.askAboutImage(bitmap, questionText)
                    }
                },
                enabled = selectedBitmap != null && questionText.isNotBlank() && !isStreaming,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentViolet),
            ) {
                Text("Ask", fontWeight = FontWeight.Bold, color = ForegroundInverse)
            }

            // Error
            if (error != null) {
                Text(
                    text = error!!,
                    color = AccentRed,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            // Streaming indicator
            if (isStreaming && response.isEmpty()) {
                StreamingDot()
            }

            // Response
            if (response.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(4.dp, 20.dp, 20.dp, 20.dp),
                    color = SurfaceCard,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    MarkdownText(
                        text = response,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun StreamingDot() {
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
            .background(AccentViolet.copy(alpha = alpha), RoundedCornerShape(4.dp)),
    )
}
