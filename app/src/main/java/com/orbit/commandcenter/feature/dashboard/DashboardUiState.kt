package com.orbit.commandcenter.feature.dashboard

import com.orbit.commandcenter.domain.model.OrbitModule

/**
 * Immutable state rendered by [DashboardScreen].
 */
data class DashboardUiState(
    val modules: List<OrbitModule> = emptyList(),
    val isLoaded: Boolean = false,
) {
    val hasModules: Boolean
        get() = modules.isNotEmpty()
}
