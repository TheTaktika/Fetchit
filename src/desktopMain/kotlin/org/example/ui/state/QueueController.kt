package org.example.ui.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.example.model.DownloadItem
import org.example.model.DownloadStatus
import org.example.service.QueueManager
import org.example.service.YtDlpService

class QueueController(
    ytDlpService: YtDlpService,
    private val logsState: LogsState
) {
    val items = mutableStateListOf<DownloadItem>()
    var tick by mutableStateOf(0)
        private set

    val manager = QueueManager(items, ytDlpService) { line ->
        logsState.append(line)
    }

    init {
        Thread.startVirtualThread {
            while (true) {
                Thread.sleep(300)
                val hasActive = items.any {
                    it.status == DownloadStatus.FETCHING ||
                            it.status == DownloadStatus.DOWNLOADING ||
                            it.status == DownloadStatus.PROCESSING
                }
                if (hasActive) {
                    tick++
                }
            }
        }
    }

    fun moveUp(item: DownloadItem) {
        val index = items.indexOf(item)
        if (index > 0) {
            val temp = items[index]
            items[index] = items[index - 1]
            items[index - 1] = temp
        }
    }

    fun moveDown(item: DownloadItem) {
        val index = items.indexOf(item)
        if (index in 0 until items.size - 1) {
            val temp = items[index]
            items[index] = items[index + 1]
            items[index + 1] = temp
        }
    }

    fun remove(item: DownloadItem) {
        if (item.status != DownloadStatus.DONE &&
            item.status != DownloadStatus.ERROR &&
            item.status != DownloadStatus.CANCELLED
        ) {
            manager.cancelItem(item)
        }
        items.remove(item)
    }

    fun forceTick() {
        tick++
    }
}