package org.example

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun Sidebar(controller: MainController) {
    NavigationRail(
        containerColor = Color.Transparent,
        modifier = Modifier
            .fillMaxHeight()
            .width(200.dp)
            .padding(top = 16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            NavigationRailItem(
                selected = controller.currentScreen == "download",
                onClick = { controller.changeScreen("download") },
                label = { Text(controller.lang.get("sidebar.download")) },
                icon = { /* Сюда позже добавим современную иконку */ }
            )

            NavigationRailItem(
                selected = controller.currentScreen == "queue",
                onClick = { controller.changeScreen("queue") },
                label = { Text(controller.lang.get("sidebar.queue")) },
                icon = { /* Иконка очереди */ }
            )

            NavigationRailItem(
                selected = controller.currentScreen == "settings",
                onClick = { controller.changeScreen("settings") },
                label = { Text(controller.lang.get("sidebar.settings")) },
                icon = { /* Иконка настроек */ }
            )

            NavigationRailItem(
                selected = controller.currentScreen == "logs",
                onClick = { controller.changeScreen("logs") },
                label = { Text(controller.lang.get("sidebar.logs")) },
                icon = { /* Иконка логов */ }
            )
        }
    }
}
