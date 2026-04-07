package com.openclaw.ai.ui.settings

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.openclaw.ai.BuildConfig
import com.openclaw.ai.data.model.ModelDownloadStatus
import com.openclaw.ai.data.model.ModelInfo
import com.openclaw.ai.data.model.ModelType
import com.openclaw.ai.data.repository.ThemeMode
import com.openclaw.ai.ui.theme.OpenClawAITheme
import com.openclaw.ai.ui.theme.customColors

private val LavenderBackground = Color(0xFFF8F5FF)
private val PurplePrimary = Color(0xFF7C3AED)
private val PurpleLight = Color(0xFF9F67FF)
private val GreenDownloaded = Color(0xFF22C55E)
private val GrayAvailable = Color(0xFF9CA3AF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val defaultModelId by viewModel.defaultModelId.collectAsState()
    val availableModels by viewModel.availableModels.collectAsState()
    val downloadStatuses by viewModel.downloadStatuses.collectAsState()
    val downloadProgress by viewModel.downloadProgress.collectAsState()
    val context = LocalContext.current

    SettingsScreenContent(
        uiState = uiState,
        themeMode = themeMode,
        defaultModelId = defaultModelId,
        availableModels = availableModels,
        downloadStatuses = downloadStatuses,
        downloadProgress = downloadProgress,
        onBack = onBack,
        onSetTheme = viewModel::setTheme,
        onSetDefaultModel = viewModel::setDefaultModel,
        onApiKeyChange = viewModel::onApiKeyChange,
        onToggleApiKeyVisibility = viewModel::toggleApiKeyVisibility,
        onSaveApiKey = viewModel::saveApiKey,
        onTestApiKey = viewModel::testApiKey,
        onDeleteModel = viewModel::deleteModel,
        onDownloadModel = viewModel::downloadModel,
        onCancelDownload = viewModel::cancelDownload,
        onShowClearDialog = viewModel::showClearConversationsDialog,
        onDismissClearDialog = viewModel::dismissClearConversationsDialog,
        onClearAllConversations = viewModel::clearAllConversations,
        onExportConversations = { viewModel.exportConversations { json -> shareJson(context, json) } },
    )
}

private fun shareJson(context: android.content.Context, json: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/json"
        putExtra(Intent.EXTRA_TEXT, json)
        putExtra(Intent.EXTRA_SUBJECT, "OpenClaw Conversations Export")
    }
    context.startActivity(Intent.createChooser(intent, "Export conversations"))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScreenContent(
    uiState: SettingsUiState,
    themeMode: ThemeMode,
    defaultModelId: String,
    availableModels: List<ModelInfo>,
    downloadStatuses: Map<String, ModelDownloadStatus>,
    downloadProgress: Map<String, Float>,
    onBack: () -> Unit,
    onSetTheme: (ThemeMode) -> Unit,
    onSetDefaultModel: (String) -> Unit,
    onApiKeyChange: (String) -> Unit,
    onToggleApiKeyVisibility: () -> Unit,
    onSaveApiKey: (String) -> Unit,
    onTestApiKey: () -> Unit,
    onDeleteModel: (String) -> Unit,
    onDownloadModel: (String) -> Unit,
    onCancelDownload: (String) -> Unit,
    onShowClearDialog: () -> Unit,
    onDismissClearDialog: () -> Unit,
    onClearAllConversations: () -> Unit,
    onExportConversations: () -> Unit,
) {
    Scaffold(
        containerColor = LavenderBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = LavenderBackground,
                ),
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // General section card
            item {
                SettingsCard {
                    CardSectionTitle("General")
                    Spacer(modifier = Modifier.height(12.dp))
                    ThemeSelector(
                        selected = themeMode,
                        onSelect = onSetTheme,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    DefaultModelSelector(
                        models = availableModels,
                        selectedModelId = defaultModelId,
                        onSelect = onSetDefaultModel,
                    )
                }
            }

            // API Keys section card
            item {
                SettingsCard {
                    CardSectionTitle("API Keys")
                    Spacer(modifier = Modifier.height(12.dp))
                    ApiKeySection(
                        apiKey = uiState.geminiApiKey,
                        isKeyVisible = uiState.isApiKeyVisible,
                        onApiKeyChange = onApiKeyChange,
                        onToggleVisibility = onToggleApiKeyVisibility,
                        onTestConnection = onTestApiKey,
                        onSave = onSaveApiKey,
                        isValid = uiState.isApiKeyValid,
                        isTesting = uiState.isTesting,
                    )
                }
            }

            // Models section card
            item {
                SettingsCard {
                    CardSectionTitle("Models")
                    Spacer(modifier = Modifier.height(8.dp))
                    if (availableModels.isEmpty()) {
                        Text(
                            text = "No models available",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        availableModels.forEachIndexed { index, model ->
                            val status = downloadStatuses[model.id] ?: if (model.isCloud) {
                                ModelDownloadStatus.DOWNLOADED
                            } else {
                                ModelDownloadStatus.NOT_DOWNLOADED
                            }
                            val progress = downloadProgress[model.id] ?: 0f
                            if (index > 0) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                )
                            }
                            ModelListItem(
                                model = model,
                                status = status,
                                progress = progress,
                                onDelete = { onDeleteModel(model.id) },
                                onDownload = { onDownloadModel(model.id) },
                                onCancel = { onCancelDownload(model.id) },
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { /* check for updates */ },
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(PurplePrimary, PurpleLight),
                                    ),
                                    shape = RoundedCornerShape(50),
                                ),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                            ),
                            shape = RoundedCornerShape(50),
                        ) {
                            Text(
                                text = "Check for Updates",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
            }

            // Data section card
            item {
                SettingsCard {
                    CardSectionTitle("Data")
                    Spacer(modifier = Modifier.height(4.dp))
                    ListItem(
                        headlineContent = { Text("Export Conversations") },
                        supportingContent = { Text("Save all conversations as JSON") },
                        leadingContent = {
                            Icon(Icons.Rounded.Share, contentDescription = null)
                        },
                        modifier = Modifier.clickableListItem(onExportConversations),
                    )
                    ListItem(
                        headlineContent = {
                            Text(
                                "Clear All Conversations",
                                color = MaterialTheme.colorScheme.error,
                            )
                        },
                        supportingContent = { Text("Permanently delete all chat history") },
                        leadingContent = {
                            Icon(
                                Icons.Rounded.DeleteForever,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                            )
                        },
                        modifier = Modifier.clickableListItem(onShowClearDialog),
                    )
                    ListItem(
                        headlineContent = { Text("Clear Cache") },
                        supportingContent = { Text("Remove temporary files") },
                        leadingContent = {
                            Icon(Icons.Rounded.Delete, contentDescription = null)
                        },
                        modifier = Modifier.clickableListItem { /* no-op for now */ },
                    )
                    ListItem(
                        headlineContent = { Text("Storage Usage") },
                        leadingContent = {
                            Icon(Icons.Rounded.Storage, contentDescription = null)
                        },
                        trailingContent = {
                            StorageUsageText(models = availableModels, statuses = downloadStatuses)
                        },
                    )
                }
            }

            // About section card
            item {
                SettingsCard {
                    CardSectionTitle("About")
                    Spacer(modifier = Modifier.height(4.dp))
                    ListItem(
                        headlineContent = { Text("Version") },
                        trailingContent = {
                            Text(
                                text = BuildConfig.VERSION_NAME,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        },
                        leadingContent = {
                            Icon(Icons.Rounded.Info, contentDescription = null)
                        },
                    )
                    ListItem(
                        headlineContent = { Text("Attribution") },
                        supportingContent = {
                            Text("Based on Google AI Edge Gallery")
                        },
                        leadingContent = {
                            Icon(Icons.Rounded.FolderOpen, contentDescription = null)
                        },
                    )
                    availableModels.filter { it.version.isNotEmpty() }.forEach { model ->
                        ListItem(
                            headlineContent = { Text(model.displayName) },
                            supportingContent = { Text("v${model.version}") },
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }

    if (uiState.showClearConversationsDialog) {
        AlertDialog(
            onDismissRequest = onDismissClearDialog,
            title = { Text("Clear All Conversations?") },
            text = {
                Text("This will permanently delete all your chat history. This action cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = onClearAllConversations,
                ) {
                    Text(
                        "Clear All",
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissClearDialog) {
                    Text("Cancel")
                }
            },
        )
    }
}

@Composable
private fun SettingsCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            content()
        }
    }
}

@Composable
private fun CardSectionTitle(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemeSelector(
    selected: ThemeMode,
    onSelect: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = "Theme",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(8.dp))
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            ThemeMode.entries.forEachIndexed { index, mode ->
                SegmentedButton(
                    selected = selected == mode,
                    onClick = { onSelect(mode) },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = ThemeMode.entries.size,
                    ),
                    icon = {
                        SegmentedButtonDefaults.Icon(active = selected == mode)
                    },
                ) {
                    Text(
                        text = mode.name.lowercase()
                            .replaceFirstChar { it.uppercase() },
                    )
                }
            }
        }
    }
}

@Composable
private fun DefaultModelSelector(
    models: List<ModelInfo>,
    selectedModelId: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedModel = models.find { it.id == selectedModelId }

    Column(modifier = modifier) {
        Text(
            text = "Default Model",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = selectedModel?.displayName ?: "Select a model",
                modifier = Modifier.weight(1f),
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            models.forEach { model ->
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(model.displayName)
                            Spacer(modifier = Modifier.width(8.dp))
                            ModelTypeBadge(type = model.type)
                        }
                    },
                    trailingIcon = if (model.id == selectedModelId) {
                        {
                            Icon(
                                Icons.Rounded.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    } else null,
                    onClick = {
                        onSelect(model.id)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun ModelTypeBadge(type: ModelType, modifier: Modifier = Modifier) {
    val (label, color) = when (type) {
        ModelType.LOCAL -> "Local" to MaterialTheme.customColors.localBadgeGreen
        ModelType.CLOUD -> "Cloud" to MaterialTheme.customColors.cloudBadgeBlue
    }
    Badge(
        containerColor = color.copy(alpha = 0.15f),
        contentColor = color,
        modifier = modifier,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 4.dp),
        )
    }
}

@Composable
private fun ModelListItem(
    model: ModelInfo,
    status: ModelDownloadStatus,
    progress: Float,
    onDelete: () -> Unit,
    onDownload: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = model.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    ModelTypeBadge(type = model.type)
                    if (model.sizeLabel.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(4.dp))
                        ModelSizeBadge(
                            label = model.sizeLabel,
                            isDownloaded = status == ModelDownloadStatus.DOWNLOADED,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    when (status) {
                        ModelDownloadStatus.DOWNLOADED -> {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                tint = GreenDownloaded,
                                modifier = Modifier.size(14.dp),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Downloaded",
                                style = MaterialTheme.typography.labelSmall,
                                color = GreenDownloaded,
                            )
                        }
                        ModelDownloadStatus.DOWNLOADING -> {
                            Text(
                                text = "Downloading… ${(progress * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        ModelDownloadStatus.FAILED -> {
                            Text(
                                text = "Download failed",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                        ModelDownloadStatus.NOT_DOWNLOADED -> {
                            Text(
                                text = "Available",
                                style = MaterialTheme.typography.labelSmall,
                                color = GrayAvailable,
                            )
                        }
                    }
                }
            }

            when (status) {
                ModelDownloadStatus.DOWNLOADED -> {
                    if (model.isLocal) {
                        IconButton(onClick = onDelete) {
                            Icon(
                                Icons.Rounded.Delete,
                                contentDescription = "Delete ${model.displayName}",
                                tint = PurplePrimary,
                            )
                        }
                    }
                }
                ModelDownloadStatus.DOWNLOADING -> {
                    IconButton(onClick = onCancel) {
                        CircularProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                        )
                    }
                }
                ModelDownloadStatus.NOT_DOWNLOADED, ModelDownloadStatus.FAILED -> {
                    if (model.isLocal) {
                        FilledTonalIconButton(onClick = onDownload) {
                            Icon(
                                Icons.Rounded.Download,
                                contentDescription = "Download ${model.displayName}",
                            )
                        }
                    } else {
                        Icon(
                            Icons.Rounded.CloudDownload,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
        if (status == ModelDownloadStatus.DOWNLOADING) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp),
            )
        }
    }
}

@Composable
private fun ModelSizeBadge(
    label: String,
    isDownloaded: Boolean,
    modifier: Modifier = Modifier,
) {
    val bgColor = if (isDownloaded) GreenDownloaded.copy(alpha = 0.15f) else GrayAvailable.copy(alpha = 0.15f)
    val textColor = if (isDownloaded) GreenDownloaded else GrayAvailable
    Badge(
        containerColor = bgColor,
        contentColor = textColor,
        modifier = modifier,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 4.dp),
        )
    }
}

@Composable
private fun StorageUsageText(
    models: List<ModelInfo>,
    statuses: Map<String, ModelDownloadStatus>,
    modifier: Modifier = Modifier,
) {
    val downloadedBytes = models
        .filter { statuses[it.id] == ModelDownloadStatus.DOWNLOADED && it.isLocal }
        .sumOf { it.downloadSizeBytes }
    val displayText = when {
        downloadedBytes == 0L -> "0 MB"
        downloadedBytes < 1_000_000L -> "${downloadedBytes / 1_000} KB"
        downloadedBytes < 1_000_000_000L -> "${downloadedBytes / 1_000_000} MB"
        else -> String.format("%.1f GB", downloadedBytes / 1_000_000_000.0)
    }
    Text(
        text = displayText,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier,
    )
}

private fun Modifier.clickableListItem(onClick: () -> Unit): Modifier =
    this.clickable(onClick = onClick)

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun SettingsScreenPreview() {
    OpenClawAITheme {
        SettingsScreenContent(
            uiState = SettingsUiState(),
            themeMode = ThemeMode.SYSTEM,
            defaultModelId = "gemini-2.0-flash",
            availableModels = com.openclaw.ai.data.model.DefaultModels.ALL,
            downloadStatuses = mapOf(
                "gemma-4-2b" to ModelDownloadStatus.NOT_DOWNLOADED,
                "gemini-2.0-flash" to ModelDownloadStatus.DOWNLOADED,
                "gemini-2.0-pro" to ModelDownloadStatus.DOWNLOADED,
            ),
            downloadProgress = emptyMap(),
            onBack = {},
            onSetTheme = {},
            onSetDefaultModel = {},
            onApiKeyChange = {},
            onToggleApiKeyVisibility = {},
            onSaveApiKey = {},
            onTestApiKey = {},
            onDeleteModel = {},
            onDownloadModel = {},
            onCancelDownload = {},
            onShowClearDialog = {},
            onDismissClearDialog = {},
            onClearAllConversations = {},
            onExportConversations = {},
        )
    }
}
