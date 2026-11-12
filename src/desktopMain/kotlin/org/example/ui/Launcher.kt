package org.example.ui

import androidx.compose.ui.window.application
import org.example.ui.screen.AppWindow

fun main(): Unit = application {
    AppWindow(onExit = { exitApplication() })
}