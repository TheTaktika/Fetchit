package org.example.ui.platform

import java.io.File
import javax.swing.JFileChooser

class DesktopFilePicker : FilePicker {
    override fun pickDirectory(title: String, initialDir: File): File? {
        val chooser = JFileChooser().apply {
            dialogTitle = title
            fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
            if (initialDir.exists()) {
                currentDirectory = initialDir
            }
        }
        return if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            chooser.selectedFile
        } else {
            null
        }
    }
}