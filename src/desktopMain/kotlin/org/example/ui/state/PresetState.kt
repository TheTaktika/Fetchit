package org.example.ui.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.example.model.Preset
import org.example.service.PresetStorage

class PresetState(private val storage: PresetStorage) {
    val list = mutableStateListOf<Preset>()
    var selected by mutableStateOf<Preset?>(null)

    fun load() {
        val loaded = storage.load()
        list.clear()
        list.addAll(loaded)
        if (selected == null || list.none { it.name == selected?.name }) {
            selected = list.firstOrNull()
        }
    }

    fun save() {
        storage.save(list.toList())
    }
}