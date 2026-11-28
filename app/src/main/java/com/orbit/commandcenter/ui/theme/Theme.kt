package com.orbit.commandcenter.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/**
 * Dark scheme — the canonical ORBIT identity: graphite background, charcoal surfaces,
 * violet primary accent, blue secondary accent, soft white text.
 */
private val OrbitDarkColorScheme = darkColorScheme(
    primary = OrbitViolet,
    onPrimary = OrbitGraphite,
    primaryContainer = OrbitSlate,
    onPrimaryContainer = OrbitSoftWhite,
    secondary = OrbitBlue,
    onSecondary = OrbitGraphite,
    secondaryContainer = OrbitSlate,
    onSecondaryContainer = OrbitSoftWhite,
    tertiary = OrbitBlue,
    onTertiary = OrbitGraphite,
    background = OrbitGraphite,
    onBackground = OrbitSoftWhite,
    surface = OrbitCharcoal,
    onSurface = OrbitSoftWhite,
    surfaceVariant = OrbitSlate,
    onSurfaceVariant = OrbitMutedText,
    surfaceContainer = OrbitCharcoal,
    outline = OrbitOutline,
    outlineVariant = OrbitOutline,
    error = OrbitError,
    onError = OrbitOnError,
    scrim = OrbitGraphite,
)

/**
 * Light scheme — the same hues on paper, with deepened accents so contrast stays AA.
 */
private val OrbitLightColorScheme = lightColorScheme(
    primary = OrbitVioletDeep,
    onPrimary = OrbitOnVioletDeep,
    primaryContainer = OrbitSlateLight,
    onPrimaryContainer = OrbitInk,
    secondary = OrbitBlueDeep,
    onSecondary = OrbitOnVioletDeep,
    secondaryContainer = OrbitSlateLight,
    onSecondaryContainer = OrbitInk,
    tertiary = OrbitBlueDeep,
    onTertiary = OrbitOnVioletDeep,
    background = OrbitPaper,
    onBackground = OrbitInk,
    surface = OrbitCardLight,
    onSurface = OrbitInk,
    surfaceVariant = OrbitSlateLight,
    onSurfaceVariant = OrbitInkMuted,
    surfaceContainer = OrbitCardLight,
    outline = OrbitOutlineLight,
    outlineVariant = OrbitOutlineLight,
    error = OrbitErrorLight,
    onError = OrbitOnErrorLight,
    scrim = OrbitInk,
)

/**
 * Applies the ORBIT Material 3 theme.
 *
 * System dynamic colour is intentionally not used: ORBIT has its own identity and the
 * palette must stay predictable across devices.
 */
@Composable
fun OrbitTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) OrbitDarkColorScheme else OrbitLightColorScheme,
        typography = OrbitTypography,
        shapes = OrbitShapes,
        content = content,
    )
}
