package org.example.ui

import androidx.compose.runtime.*
import org.example.model.*
import org.example.service.*
import org.example.ui.platform.FilePicker
import org.example.ui.state.LogsState
import org.example.ui.state.PresetState
import org.example.ui.state.QueueController
import java.io.File
import java.util.Locale

class MainController(private val filePicker: FilePicker) {
    val lang: LanguageManager = LanguageManager.getInstance()

    private val ytDlpService = YtDlpService()
    private val tempManager = TempManager(YtDlpPathResolver())

    val logs = LogsState()
    val presets = PresetState(PresetStorage())
    val queue = QueueController(ytDlpService, logs)

    var currentScreen by mutableStateOf("download")
    var urlInput by mutableStateOf("")
    var proxyInput by mutableStateOf("")
    var selectedDir by mutableStateOf(File(System.getProperty("user.home"), "Downloads/Fetchit-Downloads"))

    var isUpdating by mutableStateOf(false)
    var langVersion by mutableStateOf(0)

    init {
        if (!selectedDir.exists()) {
            selectedDir.mkdirs()
        }
        presets.load()
    }

    fun setLanguage(langCode: String) {
        val newLocale = if (langCode == "RU") Locale.of("ru") else Locale.of("en")
        lang.setLocale(newLocale)
        langVersion++
    }

    fun getCurrentLangCode(): String {
        return if (lang.currentLocale.language == "ru") "RU" else "EN"
    }

    fun changeScreen(screenName: String) {
        currentScreen = screenName
    }

    fun onDownloadClick() {
        val url = urlInput.trim()
        if (url.isEmpty()) {
            logs.append(lang.get("error.empty_url"))
            return
        }

        val isDuplicate = queue.items.any {
            it.config.url() == url &&
                    it.status != DownloadStatus.ERROR &&
                    it.status != DownloadStatus.CANCELLED
        }
        if (isDuplicate) {
            logs.append(lang.get("error.duplicate_in_queue"))
            return
        }

        val preset = presets.selected
        if (preset == null) {
            logs.append(lang.get("error.no_preset"))
            return
        }

        val config = DownloadConfig(url, selectedDir.absolutePath, proxyInput.trim(), preset)
        val item = DownloadItem(url, config)

        queue.items.add(item)
        urlInput = ""
        logs.append(lang.get("status.added_to_queue"))

        changeScreen("queue")
        queue.manager.processQueue()
    }

    fun onChooseDirClick() {
        val selected = filePicker.pickDirectory(
            title = lang.get("dialog.choose_dir_title"),
            initialDir = selectedDir
        )
        if (selected != null) {
            selectedDir = selected
        }
    }

    fun onUpdateClick() {
        if (isUpdating) return
        isUpdating = true
        logs.append("\n" + lang.get("status.checking_updates"))

        Thread.startVirtualThread {
            try {
                val code = ytDlpService.update { line -> logs.append(line) }
                logs.append(lang.get("status.done_code") + code + " ===")
            } catch (e: Exception) {
                logs.append(lang.get("status.error_prefix") + e.message + " ===")
            } finally {
                isUpdating = false
            }
        }
    }

    fun onClearCacheClick() {
        try {
            tempManager.clearCache()
            logs.append(lang.get("status.cache_cleared"))
        } catch (e: Exception) {
            logs.append(lang.get("error.cache_clear_failed") + e.message)
        }
    }
}