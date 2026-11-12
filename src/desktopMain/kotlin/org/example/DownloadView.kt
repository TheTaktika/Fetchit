package org.example

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun DownloadView(controller: MainController) {
    var presetMenuExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 30.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = controller.lang.get("view.download.title"),
            style = MaterialTheme.typography.headlineMedium
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = controller.urlInput,
                onValueChange = { controller.urlInput = it },
                placeholder = { Text(controller.lang.get("field.url")) },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )

            Button(
                onClick = { controller.onDownloadClick() },
                enabled = !controller.isUpdating,
                modifier = Modifier.height(56.dp)
            ) {
                Text(controller.lang.get("button.download"))
            }
        }

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
                            Text(controller.selectedPreset?.name ?: "—")
                        }
                        DropdownMenu(
                            expanded = presetMenuExpanded,
                            onDismissRequest = { presetMenuExpanded = false }
                        ) {
                            controller.presets.forEach { preset ->
                                DropdownMenuItem(
                                    text = { Text(preset.name) },
                                    onClick = {
                                        controller.selectedPreset = preset
                                        presetMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedButton(onClick = { controller.changeScreen("settings") }) {
                        Text(controller.lang.get("button.settings"))
                    }
                }

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
        }
    }
}
