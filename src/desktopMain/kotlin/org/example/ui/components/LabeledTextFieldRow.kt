package org.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.example.ui.kit.AppTooltip

@Composable
fun LabeledTextFieldRow(
    label: String,
    tooltip: String?,
    value: String,
    placeholder: String = "",
    onValueChange: (String) -> Unit
) {
    AppTooltip(tooltip) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(label, modifier = Modifier.width(180.dp), style = MaterialTheme.typography.bodyMedium)
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = if (placeholder.isNotEmpty()) { { Text(placeholder) } } else null,
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }
    }
}