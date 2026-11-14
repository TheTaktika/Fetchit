package org.example.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.example.model.DownloadItem
import org.example.ui.MainController
import org.example.ui.features.queue.blocks.QueueHeaderRow
import org.example.ui.features.queue.blocks.QueueItemRow
import org.example.ui.features.queue.blocks.QueueSummaryCard
import org.example.ui.features.queue.blocks.QueueErrorList

@Composable
fun QueueView(controller: MainController) {
    val tick = controller.queue.tick
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

        QueueHeaderRow(controller)

        if (controller.queue.items.isEmpty()) {
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
                items(controller.queue.items, key = { it.url }) { item ->
                    QueueItemRow(
                        item = item,
                        tick = tick,
                        isSelected = selectedItem == item,
                        controller = controller,
                        onSelect = { selectedItem = item }
                    )
                }
            }
        }

        QueueSummaryCard(
            title = controller.lang.get("tab.stats"),
            summary = selectedItem?.summary.orEmpty()
        )

        QueueErrorList(
            failedUrls = selectedItem?.failedUrls ?: emptyList(),
            controller = controller
        )
    }
}