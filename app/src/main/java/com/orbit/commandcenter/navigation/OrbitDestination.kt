package com.orbit.commandcenter.navigation

/**
 * Every destination reachable in the app, with its stable route string.
 *
 * Adding a feature means adding an entry here plus a `composable` block in
 * [OrbitNavHost] — no other file needs to change.
 */
sealed class OrbitDestination(val route: String) {
    data object Dashboard : OrbitDestination("orbit/dashboard")
    data object About : OrbitDestination("orbit/about")
}
