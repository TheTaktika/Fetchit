package org.example.ui.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class LogsState {
    var text by mutableStateOf("")
        private set

    fun append(newText: String) {
        text += if (newText.endsWith("\n")) newText else "$newText\n"
    }

    fun clear() {
        text = ""
    }
}