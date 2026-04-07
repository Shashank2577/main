package com.openclaw.ai.ui.drawer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openclaw.ai.data.db.entity.SpaceEntity

private val PurpleAccent = Color(0xFF7C3AED)
private val SectionLabelGray = Color(0xFF9CA3AF)

@Composable
fun SpaceSwitcher(
    spaces: List<SpaceEntity>,
    currentSpaceId: String,
    onSpaceSelected: (String) -> Unit,
    onCreateSpace: (name: String, emoji: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showCreateDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(
            text = "SPACES",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 2.sp,
            ),
            color = SectionLabelGray,
            modifier = Modifier.padding(start = 28.dp, top = 8.dp, bottom = 4.dp),
        )

        spaces.forEach { space ->
            val isSelected = space.id == currentSpaceId
            NavigationDrawerItem(
                label = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = space.emoji,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = space.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        )
                    }
                },
                selected = isSelected,
                onClick = { onSpaceSelected(space.id) },
                colors = NavigationDrawerItemDefaults.colors(
                    selectedContainerColor = Color(0xFFF3EEFF),
                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
                ),
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
            )
        }

        NavigationDrawerItem(
            label = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = PurpleAccent,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "+ Create Space",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PurpleAccent,
                    )
                }
            },
            selected = false,
            onClick = { showCreateDialog = true },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
        )
    }

    if (showCreateDialog) {
        CreateSpaceDialog(
            onConfirm = { name, emoji ->
                onCreateSpace(name, emoji)
                showCreateDialog = false
            },
            onDismiss = { showCreateDialog = false },
        )
    }
}

@Composable
private fun CreateSpaceDialog(
    onConfirm: (name: String, emoji: String) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var emoji by remember { mutableStateOf("\uD83D\uDCC1") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Space") },
        text = {
            Column {
                OutlinedTextField(
                    value = emoji,
                    onValueChange = { emoji = it },
                    label = { Text("Emoji") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name.trim(), emoji.trim().ifEmpty { "\uD83D\uDCC1" })
                    }
                },
                enabled = name.isNotBlank(),
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}
