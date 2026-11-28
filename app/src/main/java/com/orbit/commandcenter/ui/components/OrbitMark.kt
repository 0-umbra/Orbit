package com.orbit.commandcenter.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.cos
import kotlin.math.sin

/**
 * The ORBIT mark: an orbit ring, a core and a satellite riding the ring.
 *
 * Drawn with Compose so it stays sharp at any size, needs no image assets and adapts to
 * the active colour scheme by default.
 *
 * Everything that only depends on the size (the ring [Path], the [Stroke], the satellite
 * geometry) is built inside `drawWithCache`, so a scroll frame only replays the three draw
 * commands: no trigonometry, no path building and no allocation per frame.
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
    Spacer(
        modifier = modifier.drawWithCache {
            val shortestSide = size.minDimension
            if (shortestSide <= 0f) return@drawWithCache onDrawBehind { }

            // CacheDrawScope exposes size but not the DrawScope.center helper.
            val markCenter = Offset(x = size.width / 2f, y = size.height / 2f)

            val ringRadius = shortestSide * 0.40f
            val ringStroke = Stroke(width = shortestSide * 0.085f)
            val ringPath = Path().apply { addOval(Rect(center = markCenter, radius = ringRadius)) }

            val coreRadius = shortestSide * 0.145f
            val satelliteRadius = shortestSide * 0.072f
            val satelliteAngle = -0.73f // radians, upper-right of the ring
            val satelliteCenter = Offset(
                x = markCenter.x + ringRadius * cos(satelliteAngle),
                y = markCenter.y + ringRadius * sin(satelliteAngle),
            )

            onDrawBehind {
                drawPath(path = ringPath, color = ringColor, style = ringStroke)
                drawCircle(color = coreColor, radius = coreRadius, center = markCenter)
                drawCircle(
                    color = satelliteColor,
                    radius = satelliteRadius,
                    center = satelliteCenter,
                )
            }
        },
    )
}
