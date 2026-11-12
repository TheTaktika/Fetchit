package org.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.example.model.Preset
import org.example.ui.features.settings.blocks.PresetFormSection
import org.example.ui.features.settings.blocks.PresetSidebar
import org.example.ui.state.SettingsState

@Composable
fun SettingsView(controller: MainController) {
    val langVer = controller.langVersion
    val state = remember { SettingsState(controller) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(controller.selectedPreset) {
        state.fillFrom(controller.selectedPreset)
        statusMessage = null
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = remember(langVer) { controller.lang.get("view.settings.title") },
                style = MaterialTheme.typography.headlineMedium
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("RU", "EN").forEach { code ->
                    val isSelected = controller.getCurrentLangCode() == code
                    if (isSelected) {
                        Button(onClick = { controller.setLanguage(code) }) { Text(code) }
                    } else {
                        OutlinedButton(onClick = { controller.setLanguage(code) }) { Text(code) }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PresetSidebar(
                controller = controller,
                presets = controller.presets,
                selectedPreset = controller.selectedPreset,
                statusMessage = statusMessage,
                onPresetSelect = { controller.selectedPreset = it },
                onCreateClick = {
                    val newPreset = Preset.defaultAudioPreset().apply {
                        this.name = controller.lang.get("preset.new_name")
                    }
                    controller.presets.add(newPreset)
                    controller.selectedPreset = newPreset
                    statusMessage = null
                },
                onDeleteClick = {
                    val toRemove = controller.selectedPreset
                    if (toRemove != null) {
                        controller.presets.remove(toRemove)
                        controller.presetStorage.save(controller.presets.toList())
                        controller.loadPresets()
                        controller.selectedPreset = controller.presets.firstOrNull()
                        statusMessage = null
                    }
                },
                onSaveClick = {
                    val target = controller.selectedPreset
                    if (target == null) {
                        statusMessage = controller.lang.get("preset.alert_select")
                    } else {
                        state.applyTo(target)
                        controller.presetStorage.save(controller.presets.toList())
                        controller.loadPresets()
                        statusMessage = controller.lang.get("preset.alert_saved")
                    }
                }
            )

            PresetFormSection(
                controller = controller,
                state = state,
                modifier = Modifier.weight(1f)
            )
        }
    }
}