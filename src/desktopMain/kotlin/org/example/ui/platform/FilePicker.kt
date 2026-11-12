package org.example.ui.platform

import java.io.File

interface FilePicker {
    fun pickDirectory(title: String, initialDir: File): File?
}