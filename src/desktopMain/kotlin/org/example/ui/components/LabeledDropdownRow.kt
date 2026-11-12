package org.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.example.ui.kit.AppTooltip

@Composable
fun LabeledDropdownRow(
    label: String,
    tooltip: String?,
    selected: String,
    options: List<String>,
    displayMapper: (String) -> String = { it },
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    AppTooltip(tooltip) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(label, modifier = Modifier.width(180.dp), style = MaterialTheme.typography.bodyMedium)
            Box {
                OutlinedButton(onClick = { expanded = true }) {
                    Text(displayMapper(selected))
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    options.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(displayMapper(option)) },
                            onClick = {
                                onSelect(option)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}