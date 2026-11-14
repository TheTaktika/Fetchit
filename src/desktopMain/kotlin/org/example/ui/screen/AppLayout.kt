package org.example.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.example.ui.MainController

@Composable
fun AppLayout(controller: MainController) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        Sidebar(controller = controller)

        VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        Box(modifier = Modifier.fillMaxSize()) {
            when (controller.currentScreen) {
                "download" -> DownloadView(controller)
                "queue" -> QueueView(controller)
                "settings" -> SettingsView(controller)
                "logs" -> LogsView(controller)
                else -> {
                }
            }
        }
    }
}