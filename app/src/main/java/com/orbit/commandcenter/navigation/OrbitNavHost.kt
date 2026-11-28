package com.orbit.commandcenter.navigation

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.orbit.commandcenter.feature.about.AboutScreen
import com.orbit.commandcenter.feature.dashboard.DashboardScreen

/**
 * Screen cross-fade duration.
 *
 * `navigation-compose` fades destinations in and out with a **700 ms** animation by default
 * (`DefaultNavTransitions` in androidx.navigation.compose). That composites two full-screen
 * destinations, and animates their alpha, for ~42 frames at 60 Hz — on entry-level hardware
 * it is enough to drop frames during every single navigation.
 *
 * 180 ms keeps the same fade language while spending roughly a quarter of the frames.
 * The transitions are declared explicitly so the app never silently inherits a different
 * library default again.
 */
private const val NavFadeMillis = 180
private val NavFadeSpec: FiniteAnimationSpec<Float> = tween(durationMillis = NavFadeMillis)

/**
 * Application navigation graph.
 *
 * Phase 0 has two destinations: the dashboard (start) and the about screen.
 */
@Composable
fun OrbitApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = OrbitDestination.Dashboard.route,
        enterTransition = { fadeIn(animationSpec = NavFadeSpec) },
        exitTransition = { fadeOut(animationSpec = NavFadeSpec) },
        popEnterTransition = { fadeIn(animationSpec = NavFadeSpec) },
        popExitTransition = { fadeOut(animationSpec = NavFadeSpec) },
    ) {
        composable(OrbitDestination.Dashboard.route) {
            DashboardScreen(
                onNavigateToAbout = { navController.navigate(OrbitDestination.About.route) },
            )
        }
        composable(OrbitDestination.About.route) {
            AboutScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }
    }
}
