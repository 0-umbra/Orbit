package com.orbit.commandcenter.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.orbit.commandcenter.feature.about.AboutScreen
import com.orbit.commandcenter.feature.dashboard.DashboardScreen

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
