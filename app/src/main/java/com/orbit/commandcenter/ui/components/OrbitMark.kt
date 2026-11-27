package com.orbit.commandcenter.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.cos
import kotlin.math.sin

/**
 * The ORBIT mark: an orbit ring, a core and a satellite riding the ring.
 *
 * Drawn with Canvas so it stays sharp at any size, needs no image assets and adapts to the
 * active colour scheme by default.
 *
 * @param modifier size the mark; pass `Modifier.size(...)`.
 */
@Composable
fun OrbitMark(
    modifier: Modifier = Modifier,
    ringColor: Color = MaterialTheme.colorScheme.primary,
    coreColor: Color = MaterialTheme.colorScheme.secondary,
    satelliteColor: Color = MaterialTheme.colorScheme.onSurface,
) {
    Canvas(modifier = modifier) {
        val shortestSide = size.minDimension
        if (shortestSide <= 0f) return@Canvas

        val ringRadius = shortestSide * 0.40f
        val ringStroke = shortestSide * 0.085f
        val coreRadius = shortestSide * 0.145f
        val satelliteRadius = shortestSide * 0.072f

        drawCircle(color = ringColor, radius = ringRadius, style = Stroke(width = ringStroke))
        drawCircle(color = coreColor, radius = coreRadius)

        val satelliteAngle = -0.73f // radians, upper-right of the ring
        val satelliteCenter = Offset(
            x = center.x + ringRadius * cos(satelliteAngle),
            y = center.y + ringRadius * sin(satelliteAngle),
        )
        drawCircle(
            color = satelliteColor,
            radius = satelliteRadius,
            center = satelliteCenter,
        )
    }
}
