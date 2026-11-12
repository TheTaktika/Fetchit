package org.example.ui.kit

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppTooltip(
    tooltipText: String?,
    content: @Composable () -> Unit
) {
    if (tooltipText.isNullOrBlank()) {
        content()
    } else {
        TooltipArea(
            tooltip = {
                Surface(
                    modifier = Modifier.shadow(4.dp).widthIn(max = 400.dp),
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.inverseSurface
                ) {
                    Text(
                        text = tooltipText,
                        color = MaterialTheme.colorScheme.inverseOnSurface,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            },
            delayMillis = 300,
            content = content
        )
    }
}