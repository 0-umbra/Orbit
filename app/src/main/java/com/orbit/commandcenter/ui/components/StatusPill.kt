package com.orbit.commandcenter.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val PillContentPadding = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)

/**
 * Small status chip used to label the state of a module or a build.
 *
 * The upper-cased label and the alpha-modified colours are derived once per input with
 * [remember] instead of on every composition, so scrolling a long list does not allocate
 * strings and colours for every card that enters the viewport.
 */
@Composable
fun StatusPill(
    text: String,
    modifier: Modifier = Modifier,
    accent: Color = MaterialTheme.colorScheme.primary,
) {
    val label = remember(text) { text.uppercase() }
    val backgroundColor = remember(accent) { accent.copy(alpha = 0.14f) }
    val border = remember(accent) { BorderStroke(width = 1.dp, color = accent.copy(alpha = 0.35f)) }

    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = backgroundColor,
        contentColor = accent,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = border,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            modifier = PillContentPadding,
        )
    }
}
