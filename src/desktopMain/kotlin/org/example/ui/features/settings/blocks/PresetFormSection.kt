package org.example.ui.features.settings.blocks

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.example.ui.MainController
import org.example.ui.components.LabeledCheckboxRow
import org.example.ui.components.LabeledDropdownRow
import org.example.ui.components.LabeledTextFieldRow
import org.example.ui.state.SettingsState

@Composable
fun PresetFormSection(
    controller: MainController,
    state: SettingsState,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxHeight(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        )
    ) {
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BasicSettingsSection(controller, state)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            MediaSettingsSection(controller, state)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            SubtitlesSettingsSection(controller, state)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            AdvancedSettingsSection(controller, state)
        }
    }
}

@Composable
private fun BasicSettingsSection(controller: MainController, state: SettingsState) {
    LabeledTextFieldRow(
        label = controller.lang.get("preset.name"),
        tooltip = controller.lang.get("tooltip.name"),
        value = state.name,
        onValueChange = { state.name = it }
    )
    LabeledDropdownRow(
        label = controller.lang.get("preset.format"),
        tooltip = controller.lang.get("tooltip.format"),
        selected = state.format,
        options = listOf("audio", "video"),
        onSelect = { state.format = it }
    )
}

@Composable
private fun MediaSettingsSection(controller: MainController, state: SettingsState) {
    LabeledDropdownRow(
        label = controller.lang.get("preset.audio_format"),
        tooltip = controller.lang.get("tooltip.audio_format"),
        selected = state.audioFormat,
        options = listOf("mp3", "m4a", "flac", "opus", "wav"),
        onSelect = { state.audioFormat = it }
    )
    LabeledTextFieldRow(
        label = controller.lang.get("preset.audio_quality"),
        tooltip = controller.lang.get("tooltip.audio_quality"),
        value = state.audioQuality,
        onValueChange = { state.audioQuality = it }
    )
    LabeledDropdownRow(
        label = controller.lang.get("preset.video_quality"),
        tooltip = controller.lang.get("tooltip.video_quality"),
        selected = state.videoQuality,
        options = listOf("best", "2160p", "1440p", "1080p", "720p", "480p", "360p"),
        onSelect = { state.videoQuality = it }
    )
    LabeledDropdownRow(
        label = controller.lang.get("preset.merge_format"),
        tooltip = controller.lang.get("tooltip.merge_format"),
        selected = state.mergeFormat,
        options = listOf("mp4", "mkv", "webm"),
        onSelect = { state.mergeFormat = it }
    )
    LabeledCheckboxRow(
        text = controller.lang.get("preset.embed_thumbnail"),
        tooltip = controller.lang.get("tooltip.embed_thumbnail"),
        checked = state.embedThumbnail,
        onCheckedChange = { state.embedThumbnail = it }
    )
    LabeledCheckboxRow(
        text = controller.lang.get("preset.add_metadata"),
        tooltip = controller.lang.get("tooltip.add_metadata"),
        checked = state.addMetadata,
        onCheckedChange = { state.addMetadata = it }
    )
    LabeledCheckboxRow(
        text = controller.lang.get("preset.embed_chapters"),
        tooltip = controller.lang.get("tooltip.embed_chapters"),
        checked = state.embedChapters,
        onCheckedChange = { state.embedChapters = it }
    )
    LabeledCheckboxRow(
        text = controller.lang.get("preset.split_chapters"),
        tooltip = null,
        checked = state.splitChapters,
        onCheckedChange = { state.splitChapters = it }
    )
    LabeledCheckboxRow(
        text = controller.lang.get("preset.keep_video"),
        tooltip = controller.lang.get("tooltip.keep_video"),
        checked = state.keepVideo,
        onCheckedChange = { state.keepVideo = it }
    )
    LabeledCheckboxRow(
        text = controller.lang.get("preset.no_overwrites"),
        tooltip = controller.lang.get("tooltip.no_overwrites"),
        checked = state.noOverwrites,
        onCheckedChange = { state.noOverwrites = it }
    )
}

@Composable
private fun SubtitlesSettingsSection(controller: MainController, state: SettingsState) {
    LabeledCheckboxRow(
        text = controller.lang.get("preset.write_subs"),
        tooltip = controller.lang.get("tooltip.write_subs"),
        checked = state.writeSubs,
        onCheckedChange = { state.writeSubs = it }
    )
    LabeledCheckboxRow(
        text = controller.lang.get("preset.embed_subs"),
        tooltip = controller.lang.get("tooltip.embed_subs"),
        checked = state.embedSubs,
        onCheckedChange = { state.embedSubs = it }
    )
    LabeledTextFieldRow(
        label = controller.lang.get("preset.sub_langs"),
        tooltip = controller.lang.get("tooltip.sub_langs"),
        placeholder = controller.lang.get("preset.prompt_sublangs"),
        value = state.subLangs,
        onValueChange = { state.subLangs = it }
    )
}

@Composable
private fun AdvancedSettingsSection(controller: MainController, state: SettingsState) {
    LabeledTextFieldRow(
        label = controller.lang.get("preset.fragments"),
        tooltip = controller.lang.get("tooltip.fragments"),
        value = state.fragments,
        onValueChange = { state.fragments = it }
    )
    LabeledTextFieldRow(
        label = controller.lang.get("preset.limit_rate"),
        tooltip = controller.lang.get("tooltip.limit_rate"),
        placeholder = controller.lang.get("preset.prompt_limit_rate"),
        value = state.limitRate,
        onValueChange = { state.limitRate = it }
    )
    LabeledTextFieldRow(
        label = controller.lang.get("preset.retries"),
        tooltip = controller.lang.get("tooltip.retries"),
        value = state.retries,
        onValueChange = { state.retries = it }
    )
    LabeledTextFieldRow(
        label = controller.lang.get("preset.sponsorblock"),
        tooltip = controller.lang.get("tooltip.sponsorblock"),
        placeholder = controller.lang.get("preset.prompt_sponsorblock"),
        value = state.sponsorblockRemove,
        onValueChange = { state.sponsorblockRemove = it }
    )
    LabeledDropdownRow(
        label = controller.lang.get("preset.cookies"),
        tooltip = null,
        selected = state.cookies,
        options = listOf("none", "firefox", "brave", "opera", "vivaldi"),
        displayMapper = { item ->
            if (item == "none") controller.lang.get("preset.cookies_none") else item
        },
        onSelect = { state.cookies = it }
    )
}