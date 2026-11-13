package org.example.ui.screen

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.rememberWindowState
import com.mayakapps.compose.windowstyler.WindowBackdrop
import com.mayakapps.compose.windowstyler.WindowStyle
import org.example.ui.MainController
import org.example.ui.platform.DesktopFilePicker
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.Alignment
import org.jetbrains.compose.resources.painterResource
import org.example.fetchit.generated.resources.Res
import org.example.fetchit.generated.resources.icon

@Composable
fun AppWindow(onExit: () -> Unit) {
    val windowState = rememberWindowState(
        size = DpSize(1400.dp, 900.dp),
        position = WindowPosition(Alignment.Center)
    )
    val controller = remember {
        MainController(filePicker = DesktopFilePicker())
    }
    val darkTheme = isSystemInDarkTheme()
    val langVer = controller.langVersion
    val windowTitle = remember(langVer) { controller.lang.get("app.title") }
    val windowIcon = painterResource(Res.drawable.icon)

    Window(
        onCloseRequest = onExit,
        state = windowState,
        title = windowTitle,
        icon = windowIcon,
        undecorated = false
    ) {
        WindowStyle(
            isDarkTheme = darkTheme,
            backdropType = WindowBackdrop.Mica
        )

        MaterialTheme(
            colorScheme = if (darkTheme) darkColorScheme() else lightColorScheme()
        ) {
            AppLayout(controller = controller)
        }
    }
}