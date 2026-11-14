package org.example.ui.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.example.ui.MainController
import org.example.model.Preset

class SettingsState(private val controller: MainController) {

    var name by mutableStateOf("")
    var format by mutableStateOf("audio")
    var audioFormat by mutableStateOf("mp3")
    var audioQuality by mutableStateOf("0")
    var videoQuality by mutableStateOf("1080p")
    var mergeFormat by mutableStateOf("mp4")
    var embedThumbnail by mutableStateOf(false)
    var addMetadata by mutableStateOf(false)
    var embedChapters by mutableStateOf(false)
    var writeSubs by mutableStateOf(false)
    var embedSubs by mutableStateOf(false)
    var subLangs by mutableStateOf("")
    var fragments by mutableStateOf("1")
    var limitRate by mutableStateOf("")
    var retries by mutableStateOf("10")
    var sponsorblockRemove by mutableStateOf("")
    var keepVideo by mutableStateOf(false)
    var noOverwrites by mutableStateOf(false)
    var cookies by mutableStateOf("none")
    var splitChapters by mutableStateOf(false)

    fun fillFrom(p: Preset?) {
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

    fun applyTo(p: Preset) {
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
}