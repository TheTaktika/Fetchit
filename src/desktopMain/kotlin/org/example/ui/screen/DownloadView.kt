package org.example.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.example.ui.MainController
import org.example.ui.features.download.blocks.DownloadSettingsCard
import org.example.ui.features.download.blocks.UrlInputBlock

@Composable
fun DownloadView(controller: MainController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 30.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = controller.lang.get("view.download.title"),
            style = MaterialTheme.typography.headlineMedium
        )

        UrlInputBlock(
            url = controller.urlInput,
            onUrlChange = { controller.urlInput = it },
            onDownloadClick = { controller.onDownloadClick() },
            isDownloadEnabled = !controller.isUpdating,
            placeholder = controller.lang.get("field.url"),
            buttonText = controller.lang.get("button.download")
        )

        DownloadSettingsCard(
            controller = controller,
            onSettingsClick = { controller.changeScreen("settings") }
        )
    }
}