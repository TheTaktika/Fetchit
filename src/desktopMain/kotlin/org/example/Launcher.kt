package org.example

import androidx.compose.ui.window.application

fun main(): Unit = application {
    AppWindow(onExit = { exitApplication() })
}