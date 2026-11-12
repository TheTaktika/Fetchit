package org.example

import androidx.compose.foundation.ContextMenuArea
import androidx.compose.foundation.ContextMenuItem
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.model.DownloadItem
import org.example.model.DownloadStatus
import org.jetbrains.skia.Image as SkiaImage

@Composable
fun QueueView(controller: MainController) {
    val tick = controller.queueTick
    var selectedItem by remember { mutableStateOf<DownloadItem?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = controller.lang.get("sidebar.queue"),
            style = MaterialTheme.typography.headlineMedium
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(controller.lang.get("column.thumbnail"), Modifier.width(100.dp), fontWeight = FontWeight.SemiBold)
            Text(controller.lang.get("column.title"), Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
            Text(controller.lang.get("column.status"), Modifier.width(120.dp), fontWeight = FontWeight.SemiBold)
            Text(controller.lang.get("column.progress"), Modifier.width(150.dp), fontWeight = FontWeight.SemiBold)
            Text(controller.lang.get("column.speed"), Modifier.width(100.dp), fontWeight = FontWeight.SemiBold)
            Text(controller.lang.get("column.eta"), Modifier.width(80.dp), fontWeight = FontWeight.SemiBold)
        }

        if (controller.downloadQueue.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = controller.lang.get("status.queue_empty"),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(controller.downloadQueue, key = { it.url }) { item ->
                    QueueRow(
                        item = item,
                        tick = tick,
                        isSelected = selectedItem == item,
                        controller = controller,
                        onSelect = { selectedItem = item }
                    )
                }
            }
        }

        val summary = selectedItem?.summary.orEmpty()
        if (summary.isNotBlank()) {
            Surface(
                modifier = Modifier.fillMaxWidth().heightIn(max = 140.dp),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = controller.lang.get("tab.stats"),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = summary,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun QueueRow(
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

    val rowColor = when (status) {
        DownloadStatus.DOWNLOADING, DownloadStatus.FETCHING, DownloadStatus.PROCESSING -> Color(0xFF1E3A5F).copy(alpha = 0.55f)
        DownloadStatus.DONE -> Color(0xFF1E4620).copy(alpha = 0.55f)
        DownloadStatus.ERROR -> Color(0xFF5C1A1A).copy(alpha = 0.55f)
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
    }

    ContextMenuArea(
        items = {
            buildList {
                if (item.canPause()) {
                    add(ContextMenuItem(controller.lang.get("menu.pause")) {
                        controller.queueManager.pauseItem(item)
                        controller.queueTick++
                    })
                }
                if (item.canResume()) {
                    add(ContextMenuItem(controller.lang.get("menu.resume")) {
                        controller.queueManager.resumeItem(item)
                        controller.queueTick++
                    })
                }
                if (item.canCancel()) {
                    add(ContextMenuItem(controller.lang.get("menu.cancel")) {
                        controller.queueManager.cancelItem(item)
                        controller.queueTick++
                    })
                }
            }
        }
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
            val bitmap: ImageBitmap? = remember(thumbBytes) {
                thumbBytes?.let {
                    runCatching { SkiaImage.makeFromEncoded(it).toComposeImageBitmap() }.getOrNull()
                }
            }
            Box(
                modifier = Modifier
                    .width(100.dp)
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

            Text(
                text = title,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = controller.lang.get("status." + status.name.lowercase()),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.width(120.dp)
            )

            Column(modifier = Modifier.width(150.dp)) {
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

            Text(
                text = speed,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.width(100.dp)
            )

            Text(
                text = eta,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.width(80.dp)
            )
        }
    }
}
