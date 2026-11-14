package org.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.example.ui.kit.AppTooltip

@Composable
fun LabeledCheckboxRow(
    text: String,
    tooltip: String?,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    AppTooltip(tooltip) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onCheckedChange(!checked) }
                .padding(vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Checkbox(checked = checked, onCheckedChange = onCheckedChange)
            Text(text, style = MaterialTheme.typography.bodyMedium)
        }
    }
}