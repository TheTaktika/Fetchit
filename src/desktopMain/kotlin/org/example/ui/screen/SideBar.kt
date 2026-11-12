package org.example.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.example.ui.MainController

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
            navDestinations.forEach { destination ->
                NavigationRailItem(
                    selected = controller.currentScreen == destination.id,
                    onClick = { controller.changeScreen(destination.id) },
                    label = { Text(controller.lang.get(destination.labelKey)) },
                    icon = {
                        //TODO: add new icons
                    }
                )
            }
        }
    }
}

data class NavDestination(
    val id: String,
    val labelKey: String
)

val navDestinations = listOf(
    NavDestination("download", "sidebar.download"),
    NavDestination("queue", "sidebar.queue"),
    NavDestination("settings", "sidebar.settings"),
    NavDestination("logs", "sidebar.logs")
)
