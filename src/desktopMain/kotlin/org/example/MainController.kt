package org.example

import androidx.compose.runtime.*
import org.example.model.*
import org.example.service.*
import org.example.ui.LanguageManager
import java.io.File
import javax.swing.JFileChooser

class MainController {
    val lang: LanguageManager = LanguageManager.getInstance()

    val ytDlpService = YtDlpService()
    val presetStorage = PresetStorage()
    val tempManager = TempManager(YtDlpPathResolver())

    var currentScreen by mutableStateOf("download")
    var urlInput by mutableStateOf("")
    var proxyInput by mutableStateOf("")
    var selectedDir by mutableStateOf(File(System.getProperty("user.home"), "Downloads/Fetchit-Downloads"))

    val presets = mutableStateListOf<Preset>()
    var selectedPreset by mutableStateOf<Preset?>(null)

    val downloadQueue = mutableStateListOf<DownloadItem>()
    var logsText by mutableStateOf("")
    var isUpdating by mutableStateOf(false)

    val queueManager: QueueManager

    init {
        if (!selectedDir.exists()) {
            selectedDir.mkdirs()
        }
        loadPresets()
        queueManager = QueueManager(downloadQueue, ytDlpService) { line ->
            appendLog(line)
        }
    }

    fun changeScreen(screenName: String) {
        currentScreen = screenName
    }

    fun loadPresets() {
        val loaded = presetStorage.load()
        presets.clear()
        presets.addAll(loaded)
        if (selectedPreset == null || presets.none { it.name == selectedPreset?.name }) {
            selectedPreset = presets.firstOrNull()
        }
    }

    fun appendLog(text: String) {
        logsText += if (text.endsWith("\n")) text else "$text\n"
    }

    fun onDownloadClick() {
        val url = urlInput.trim()
        if (url.isEmpty()) {
            appendLog(lang.get("error.empty_url"))
            return
        }

        val isDuplicate = downloadQueue.any {
            it.config.url() == url &&
                    it.status != DownloadStatus.ERROR &&
                    it.status != DownloadStatus.CANCELLED
        }
        if (isDuplicate) {
            appendLog(lang.get("error.duplicate_in_queue"))
            return
        }

        val preset = selectedPreset
        if (preset == null) {
            appendLog(lang.get("error.no_preset"))
            return
        }

        val config = DownloadConfig(
            url,
            selectedDir.absolutePath,
            proxyInput.trim(),
            preset
        )

        val item = DownloadItem(url, config)
        downloadQueue.add(item)
        urlInput = ""
        appendLog(lang.get("status.added_to_queue"))

        changeScreen("queue")
        queueManager.processQueue()
    }

    fun onChooseDirClick() {
        val chooser = JFileChooser().apply {
            dialogTitle = lang.get("dialog.choose_dir_title")
            fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
            if (selectedDir.exists()) {
                currentDirectory = selectedDir
            }
        }
        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            selectedDir = chooser.selectedFile
        }
    }

    fun onUpdateClick() {
        if (isUpdating) return
        isUpdating = true
        appendLog("\n" + lang.get("status.checking_updates"))

        Thread.startVirtualThread {
            try {
                val code = ytDlpService.update { line ->
                    appendLog(line)
                }
                appendLog(lang.get("status.done_code") + code + " ===")
            } catch (e: Exception) {
                appendLog(lang.get("status.error_prefix") + e.message + " ===")
            } finally {
                isUpdating = false
            }
        }
    }

    fun onClearCacheClick() {
        try {
            tempManager.clearCache()
            appendLog(lang.get("status.cache_cleared"))
        } catch (e: Exception) {
            appendLog(lang.get("error.cache_clear_failed") + e.message)
        }
    }
}
