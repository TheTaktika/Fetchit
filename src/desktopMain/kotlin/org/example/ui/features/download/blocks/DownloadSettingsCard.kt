package org.example.ui.features.download.blocks

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.example.ui.MainController

@Composable
fun DownloadSettingsCard(
    controller: MainController,
    onSettingsClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DownloadTargetRow(
                controller = controller,
                onSettingsClick = onSettingsClick
            )

            AdvancedActionsRow(
                controller = controller
            )
        }
    }
}

@Composable
private fun DownloadTargetRow(
    controller: MainController,
    onSettingsClick: () -> Unit
) {
    var presetMenuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        OutlinedButton(onClick = { controller.onChooseDirClick() }) {
            Text(controller.lang.get("button.folder"))
        }

        Text(
            text = controller.selectedDir.absolutePath,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = controller.lang.get("label.preset"),
            style = MaterialTheme.typography.labelLarge
        )

        Box {
            OutlinedButton(onClick = { presetMenuExpanded = true }) {
                Text(controller.presets.selected?.name ?: "—")
            }
            DropdownMenu(
                expanded = presetMenuExpanded,
                onDismissRequest = { presetMenuExpanded = false }
            ) {
                controller.presets.list.forEach { preset ->
                    DropdownMenuItem(
                        text = { Text(preset.name ?: "") },
                        onClick = {
                            controller.presets.selected = preset
                            presetMenuExpanded = false
                        }
                    )
                }
            }
        }

        OutlinedButton(onClick = onSettingsClick) {
            Text(controller.lang.get("button.settings"))
        }
    }
}

@Composable
private fun AdvancedActionsRow(
    controller: MainController
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        OutlinedTextField(
            value = controller.proxyInput,
            onValueChange = { controller.proxyInput = it },
            placeholder = { Text(controller.lang.get("field.proxy")) },
            singleLine = true,
            modifier = Modifier.width(300.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        OutlinedButton(
            onClick = { controller.onUpdateClick() },
            enabled = !controller.isUpdating
        ) {
            Text(controller.lang.get("button.update"))
        }

        OutlinedButton(onClick = { controller.onClearCacheClick() }) {
            Text(controller.lang.get("button.clear_cache"))
        }
    }
}