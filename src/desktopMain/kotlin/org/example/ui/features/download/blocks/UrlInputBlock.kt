package org.example.ui.features.download.blocks

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun UrlInputBlock(
    url: String,
    onUrlChange: (String) -> Unit,
    onDownloadClick: () -> Unit,
    isDownloadEnabled: Boolean,
    placeholder: String,
    buttonText: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        OutlinedTextField(
            value = url,
            onValueChange = onUrlChange,
            placeholder = { Text(placeholder) },
            singleLine = true,
            modifier = Modifier.weight(1f)
        )

        Button(
            onClick = onDownloadClick,
            enabled = isDownloadEnabled,
            modifier = Modifier.height(56.dp)
        ) {
            Text(buttonText)
        }
    }
}