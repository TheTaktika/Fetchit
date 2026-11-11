package org.example

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.rememberWindowState
import com.mayakapps.compose.windowstyler.WindowStyle
import com.mayakapps.compose.windowstyler.WindowBackdrop
import org.example.model.DownloadItem
import org.example.ui.LanguageManager

class MainController {
    val lang: LanguageManager = LanguageManager.getInstance()
    var currentScreen by mutableStateOf("download")
    val downloadQueue = mutableStateListOf<DownloadItem>()

    fun changeScreen(screenName: String) {
        currentScreen = screenName
    }
}

@Composable
fun AppWindow(onExit: () -> Unit) {
    val windowState = rememberWindowState()
    val controller = remember { MainController() }
    val darkTheme = isSystemInDarkTheme()

    Window(
        onCloseRequest = onExit,
        state = windowState,
        title = "FetchIt",
        undecorated = false
    ) {
        WindowStyle(
            isDarkTheme = darkTheme,
            backdropType = WindowBackdrop.Mica
        )

        MaterialTheme(
            colorScheme = if (darkTheme) darkColorScheme() else lightColorScheme()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent)
            ) {
                Sidebar(controller = controller)

                VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    when (controller.currentScreen) {
                        "download" -> Text(controller.lang.get("view.download.title"))
                        "queue" -> Text(controller.lang.get("sidebar.queue"))
                        "settings" -> Text(controller.lang.get("view.settings.title"))
                        "logs" -> Text(controller.lang.get("tab.logs"))
                    }
                }
            }
        }
    }
}
