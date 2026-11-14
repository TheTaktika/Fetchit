package org.example.ui.features.queue.blocks

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.example.ui.MainController

object QueueColumns {
    val Thumbnail = 100.dp
    val Status = 120.dp
    val Progress = 150.dp
    val Speed = 100.dp
    val Eta = 80.dp
}

@Composable
fun QueueHeaderRow(controller: MainController) {
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
        Text(controller.lang.get("column.thumbnail"), Modifier.width(QueueColumns.Thumbnail), fontWeight = FontWeight.SemiBold)
        Text(controller.lang.get("column.title"), Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
        Text(controller.lang.get("column.status"), Modifier.width(QueueColumns.Status), fontWeight = FontWeight.SemiBold)
        Text(controller.lang.get("column.progress"), Modifier.width(QueueColumns.Progress), fontWeight = FontWeight.SemiBold)
        Text(controller.lang.get("column.speed"), Modifier.width(QueueColumns.Speed), fontWeight = FontWeight.SemiBold)
        Text(controller.lang.get("column.eta"), Modifier.width(QueueColumns.Eta), fontWeight = FontWeight.SemiBold)
    }
}