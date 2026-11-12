package org.example

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.example.model.Preset

@Composable
fun SettingsView(controller: MainController) {
    val langVer = controller.langVersion

    var selectedPreset by remember { mutableStateOf<Preset?>(controller.presets.firstOrNull()) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    var name by remember { mutableStateOf("") }
    var format by remember { mutableStateOf("audio") }
    var audioFormat by remember { mutableStateOf("mp3") }
    var audioQuality by remember { mutableStateOf("0") }
    var videoQuality by remember { mutableStateOf("1080p") }
    var mergeFormat by remember { mutableStateOf("mp4") }
    var embedThumbnail by remember { mutableStateOf(false) }
    var addMetadata by remember { mutableStateOf(false) }
    var embedChapters by remember { mutableStateOf(false) }
    var writeSubs by remember { mutableStateOf(false) }
    var embedSubs by remember { mutableStateOf(false) }
    var subLangs by remember { mutableStateOf("") }
    var fragments by remember { mutableStateOf("1") }
    var limitRate by remember { mutableStateOf("") }
    var retries by remember { mutableStateOf("10") }
    var sponsorblockRemove by remember { mutableStateOf("") }
    var keepVideo by remember { mutableStateOf(false) }
    var noOverwrites by remember { mutableStateOf(false) }
    var cookies by remember { mutableStateOf("none") }
    var splitChapters by remember { mutableStateOf(false) }

    fun fillForm(p: Preset?) {
        if (p == null) return
        name = p.name ?: ""
        format = p.format ?: "audio"
        audioFormat = p.audioFormat ?: "mp3"
        audioQuality = p.audioQuality.toString()
        videoQuality = p.videoQuality ?: "1080p"
        mergeFormat = p.mergeOutputFormat ?: "mp4"
        embedThumbnail = p.isEmbedThumbnail
        addMetadata = p.isAddMetadata
        embedChapters = p.isEmbedChapters
        writeSubs = p.isWriteSubtitles
        embedSubs = p.isEmbedSubtitles
        subLangs = p.subtitleLanguages ?: ""
        fragments = p.concurrentFragments.toString()
        limitRate = p.limitRate ?: ""
        retries = p.retries.toString()
        sponsorblockRemove = p.sponsorblockRemove ?: ""
        keepVideo = p.isKeepVideo
        noOverwrites = p.isNoOverwrites
        cookies = p.cookiesFromBrowser ?: "none"
        splitChapters = p.isSplitChapters
    }

    LaunchedEffect(selectedPreset) {
        fillForm(selectedPreset)
    }

    fun applyForm(p: Preset) {
        p.name = name.trim().ifEmpty { controller.lang.get("preset.no_name") }
        p.format = format
        p.audioFormat = audioFormat
        p.audioQuality = audioQuality.toIntOrNull()?.coerceIn(0, 10) ?: 0
        p.videoQuality = videoQuality
        p.mergeOutputFormat = mergeFormat
        p.isEmbedThumbnail = embedThumbnail
        p.isAddMetadata = addMetadata
        p.isEmbedChapters = embedChapters
        p.isWriteSubtitles = writeSubs
        p.isEmbedSubtitles = embedSubs
        p.subtitleLanguages = subLangs.trim().ifEmpty { null }
        p.concurrentFragments = fragments.toIntOrNull()?.coerceIn(1, 16) ?: 1
        p.limitRate = limitRate.trim().ifEmpty { null }
        p.retries = retries.toIntOrNull()?.coerceIn(0, 50) ?: 10
        p.sponsorblockRemove = sponsorblockRemove.trim().ifEmpty { null }
        p.isKeepVideo = keepVideo
        p.isNoOverwrites = noOverwrites
        p.cookiesFromBrowser = if (cookies == "none") null else cookies
        p.isSplitChapters = splitChapters
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
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
            Card(
                modifier = Modifier.width(240.dp).fillMaxHeight(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                )
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = controller.lang.get("preset.title"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(controller.presets) { preset ->
                            val isSelected = preset == selectedPreset
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                        else MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)
                                    )
                                    .clickable {
                                        selectedPreset = preset
                                        statusMessage = null
                                    }
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text = preset.name ?: "",
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    OutlinedButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            val newPreset = Preset.defaultAudioPreset().apply {
                                this.name = controller.lang.get("preset.new_name")
                            }
                            controller.presets.add(newPreset)
                            selectedPreset = newPreset
                            statusMessage = null
                        }
                    ) {
                        Text(controller.lang.get("preset.btn_create"))
                    }

                    OutlinedButton(
                        modifier = Modifier.fillMaxWidth(),
                        enabled = selectedPreset != null,
                        onClick = {
                            val toRemove = selectedPreset ?: return@OutlinedButton
                            controller.presets.remove(toRemove)
                            controller.presetStorage.save(controller.presets.toList())
                            controller.loadPresets()
                            selectedPreset = controller.presets.firstOrNull()
                            statusMessage = null
                        }
                    ) {
                        Text(controller.lang.get("preset.btn_delete"))
                    }

                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {

                            val target = selectedPreset
                            if (target == null) {
                                statusMessage = controller.lang.get("preset.alert_select")
                            } else {
                                applyForm(target)
                                controller.presetStorage.save(controller.presets.toList())
                                controller.loadPresets()
                                statusMessage = controller.lang.get("preset.alert_saved")
                            }
                        }
                    ) {
                        Text(controller.lang.get("preset.btn_save"))
                    }
                    statusMessage?.let { msg ->
                        Text(
                            text = msg,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            Card(
                modifier = Modifier.weight(1f).fillMaxHeight(),
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
                    PresetTextFieldRow(
                        label = controller.lang.get("preset.name"),
                        tooltip = controller.lang.get("tooltip.name"),
                        value = name,
                        onValueChange = { name = it }
                    )
                    PresetDropdownRow(
                        label = controller.lang.get("preset.format"),
                        tooltip = controller.lang.get("tooltip.format"),
                        selected = format,
                        options = listOf("audio", "video"),
                        onSelect = { format = it }
                    )
                    PresetDropdownRow(
                        label = controller.lang.get("preset.audio_format"),
                        tooltip = controller.lang.get("tooltip.audio_format"),
                        selected = audioFormat,
                        options = listOf("mp3", "m4a", "flac", "opus", "wav"),
                        onSelect = { audioFormat = it }
                    )
                    PresetTextFieldRow(
                        label = controller.lang.get("preset.audio_quality"),
                        tooltip = controller.lang.get("tooltip.audio_quality"),
                        value = audioQuality,
                        onValueChange = { audioQuality = it }
                    )
                    PresetDropdownRow(
                        label = controller.lang.get("preset.video_quality"),
                        tooltip = controller.lang.get("tooltip.video_quality"),
                        selected = videoQuality,
                        options = listOf("best", "2160p", "1440p", "1080p", "720p", "480p", "360p"),
                        onSelect = { videoQuality = it }
                    )
                    PresetDropdownRow(
                        label = controller.lang.get("preset.merge_format"),
                        tooltip = controller.lang.get("tooltip.merge_format"),
                        selected = mergeFormat,
                        options = listOf("mp4", "mkv", "webm"),
                        onSelect = { mergeFormat = it }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    PresetCheckboxRow(
                        text = controller.lang.get("preset.embed_thumbnail"),
                        tooltip = controller.lang.get("tooltip.embed_thumbnail"),
                        checked = embedThumbnail,
                        onCheckedChange = { embedThumbnail = it }
                    )
                    PresetCheckboxRow(
                        text = controller.lang.get("preset.add_metadata"),
                        tooltip = controller.lang.get("tooltip.add_metadata"),
                        checked = addMetadata,
                        onCheckedChange = { addMetadata = it }
                    )
                    PresetCheckboxRow(
                        text = controller.lang.get("preset.embed_chapters"),
                        tooltip = controller.lang.get("tooltip.embed_chapters"),
                        checked = embedChapters,
                        onCheckedChange = { embedChapters = it }
                    )
                    PresetCheckboxRow(
                        text = controller.lang.get("preset.split_chapters"),
                        tooltip = null,
                        checked = splitChapters,
                        onCheckedChange = { splitChapters = it }
                    )
                    PresetCheckboxRow(
                        text = controller.lang.get("preset.write_subs"),
                        tooltip = controller.lang.get("tooltip.write_subs"),
                        checked = writeSubs,
                        onCheckedChange = { writeSubs = it }
                    )
                    PresetCheckboxRow(
                        text = controller.lang.get("preset.embed_subs"),
                        tooltip = controller.lang.get("tooltip.embed_subs"),
                        checked = embedSubs,
                        onCheckedChange = { embedSubs = it }
                    )
                    PresetTextFieldRow(
                        label = controller.lang.get("preset.sub_langs"),
                        tooltip = controller.lang.get("tooltip.sub_langs"),
                        placeholder = controller.lang.get("preset.prompt_sublangs"),
                        value = subLangs,
                        onValueChange = { subLangs = it }
                    )
                    PresetTextFieldRow(
                        label = controller.lang.get("preset.fragments"),
                        tooltip = controller.lang.get("tooltip.fragments"),
                        value = fragments,
                        onValueChange = { fragments = it }
                    )
                    PresetTextFieldRow(
                        label = controller.lang.get("preset.limit_rate"),
                        tooltip = controller.lang.get("tooltip.limit_rate"),
                        placeholder = controller.lang.get("preset.prompt_limit_rate"),
                        value = limitRate,
                        onValueChange = { limitRate = it }
                    )
                    PresetTextFieldRow(
                        label = controller.lang.get("preset.retries"),
                        tooltip = controller.lang.get("tooltip.retries"),
                        value = retries,
                        onValueChange = { retries = it }
                    )
                    PresetTextFieldRow(
                        label = controller.lang.get("preset.sponsorblock"),
                        tooltip = controller.lang.get("tooltip.sponsorblock"),
                        placeholder = controller.lang.get("preset.prompt_sponsorblock"),
                        value = sponsorblockRemove,
                        onValueChange = { sponsorblockRemove = it }
                    )
                    PresetCheckboxRow(
                        text = controller.lang.get("preset.keep_video"),
                        tooltip = controller.lang.get("tooltip.keep_video"),
                        checked = keepVideo,
                        onCheckedChange = { keepVideo = it }
                    )
                    PresetCheckboxRow(
                        text = controller.lang.get("preset.no_overwrites"),
                        tooltip = controller.lang.get("tooltip.no_overwrites"),
                        checked = noOverwrites,
                        onCheckedChange = { noOverwrites = it }
                    )
                    PresetDropdownRow(
                        label = controller.lang.get("preset.cookies"),
                        tooltip = null,
                        selected = cookies,
                        options = listOf("none", "firefox", "brave", "opera", "vivaldi"),
                        displayMapper = { item ->
                            if (item == "none") controller.lang.get("preset.cookies_none") else item
                        },
                        onSelect = { cookies = it }
                    )
                }
            }
        }
    }
}
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WithTooltip(tooltipText: String?, content: @Composable () -> Unit) {
    if (tooltipText.isNullOrBlank()) {
        content()
    } else {
        TooltipArea(
            tooltip = {
                Surface(
                    modifier = Modifier.shadow(4.dp).widthIn(max = 400.dp),
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.inverseSurface
                ) {
                    Text(
                        text = tooltipText,
                        color = MaterialTheme.colorScheme.inverseOnSurface,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            },
            delayMillis = 300,
            content = content
        )
    }
}
@Composable
private fun PresetTextFieldRow(
    label: String,
    tooltip: String?,
    value: String,
    placeholder: String = "",
    onValueChange: (String) -> Unit
) {
    WithTooltip(tooltip) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(label, modifier = Modifier.width(180.dp), style = MaterialTheme.typography.bodyMedium)
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = if (placeholder.isNotEmpty()) { { Text(placeholder) } } else null,
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
@Composable
private fun PresetDropdownRow(
    label: String,
    tooltip: String?,
    selected: String,
    options: List<String>,
    displayMapper: (String) -> String = { it },
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    WithTooltip(tooltip) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(label, modifier = Modifier.width(180.dp), style = MaterialTheme.typography.bodyMedium)
            Box {
                OutlinedButton(onClick = { expanded = true }) {
                    Text(displayMapper(selected))
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    options.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(displayMapper(option)) },
                            onClick = {
                                onSelect(option)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}
@Composable
private fun PresetCheckboxRow(
    text: String,
    tooltip: String?,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    WithTooltip(tooltip) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onCheckedChange(!checked) }
                .padding(vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Checkbox(checked = checked, onCheckedChange = onCheckedChange)
            Text(text, style = MaterialTheme.typography.bodyMedium)
        }
    }
}