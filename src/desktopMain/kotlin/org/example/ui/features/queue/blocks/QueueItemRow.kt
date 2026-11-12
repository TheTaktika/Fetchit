package org.example.ui.features.queue.blocks

import androidx.compose.foundation.ContextMenuArea
import androidx.compose.foundation.ContextMenuItem
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.example.model.DownloadItem
import org.example.model.DownloadStatus
import org.example.ui.MainController
import org.jetbrains.skia.Image as SkiaImage

@Composable
fun QueueItemRow(
    item: DownloadItem,
    tick: Int,
    isSelected: Boolean,
    controller: MainController,
    onSelect: () -> Unit
) {
    val status = remember(tick, item.status) { item.status }
    val progress = remember(tick, item.progress) { item.progress }
    val speed = remember(tick, item.speed) { item.speed }
    val eta = remember(tick, item.eta) { item.eta }
    val title = remember(tick, item.title) { item.title }
    val thumbBytes = remember(tick, item.thumbnailData) { item.thumbnailData }

    val index = controller.queue.items.indexOf(item)

    val rowColor = when (status) {
        DownloadStatus.DOWNLOADING, DownloadStatus.FETCHING, DownloadStatus.PROCESSING -> Color(0xFF1E3A5F).copy(alpha = 0.55f)
        DownloadStatus.DONE -> Color(0xFF1E4620).copy(alpha = 0.55f)
        DownloadStatus.ERROR -> Color(0xFF5C1A1A).copy(alpha = 0.55f)
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
    }

    ContextMenuArea(
        items = { buildQueueContextMenu(item, index, controller) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(if (isSelected) rowColor.copy(alpha = 0.85f) else rowColor)
                .clickable { onSelect() }
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QueueThumbnail(thumbBytes)
            QueueTitle(title = title, modifier = Modifier.weight(1f))
            QueueStatus(status, controller)
            QueueProgress(progress)
            QueueTextValue(speed, QueueColumns.Speed)
            QueueTextValue(eta, QueueColumns.Eta)
        }
    }
}

private fun buildQueueContextMenu(
    item: DownloadItem,
    index: Int,
    controller: MainController
): List<ContextMenuItem> {
    return buildList {
        if (item.canPause()) {
            add(ContextMenuItem(controller.lang.get("menu.pause")) {
                controller.queue.manager.pauseItem(item)
                controller.queue.forceTick()
            })
        }
        if (item.canResume()) {
            add(ContextMenuItem(controller.lang.get("menu.resume")) {
                controller.queue.manager.resumeItem(item)
                controller.queue.forceTick()
            })
        }
        if (item.canCancel()) {
            add(ContextMenuItem(controller.lang.get("menu.cancel")) {
                controller.queue.manager.cancelItem(item)
                controller.queue.forceTick()
            })
        }
        if (index > 0) {
            add(ContextMenuItem(controller.lang.get("menu.move_up")) {
                controller.queue.moveUp(item)
            })
        }
        if (index in 0 until controller.queue.items.size - 1) {
            add(ContextMenuItem(controller.lang.get("menu.move_down")) {
                controller.queue.moveDown(item)
            })
        }
        add(ContextMenuItem(controller.lang.get("menu.remove")) {
            controller.queue.remove(item)
        })
    }
}

@Composable
private fun QueueThumbnail(thumbBytes: ByteArray?) {
    val bitmap: ImageBitmap? = remember(thumbBytes) {
        thumbBytes?.let {
            runCatching { SkiaImage.makeFromEncoded(it).toComposeImageBitmap() }.getOrNull()
        }
    }
    Box(
        modifier = Modifier
            .width(QueueColumns.Thumbnail)
            .height(56.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Color.Black.copy(alpha = 0.2f)),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun QueueTitle(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        style = MaterialTheme.typography.bodyMedium,
        modifier = modifier
    )
}

@Composable
private fun QueueStatus(status: DownloadStatus, controller: MainController) {
    Text(
        text = controller.lang.get("status." + status.name.lowercase()),
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.width(QueueColumns.Status)
    )
}

@Composable
private fun QueueProgress(progress: Double) {
    Column(modifier = Modifier.width(QueueColumns.Progress)) {
        LinearProgressIndicator(
            progress = { (progress / 100.0).toFloat().coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = String.format("%.1f%%", progress),
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
private fun QueueTextValue(value: String, width: androidx.compose.ui.unit.Dp) {
    Text(
        text = value,
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.width(width)
    )
}