package com.orbit.commandcenter.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp

/**
 * The 1dp outline shared by every ORBIT card.
 *
 * [BorderStroke] is a plain class, so `BorderStroke(1.dp, color)` inside a composable
 * allocates on every composition. Hoisting it through [remember] keeps the allocation out
 * of the composition pass — which matters when cards are composed as they scroll into the
 * viewport on entry-level devices.
 */
@Composable
fun rememberOrbitCardBorder(): BorderStroke {
    val outlineVariant = MaterialTheme.colorScheme.outlineVariant
    return remember(outlineVariant) { BorderStroke(width = 1.dp, color = outlineVariant) }
}
