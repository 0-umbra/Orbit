package com.orbit.commandcenter.feature.dashboard

import androidx.compose.runtime.Immutable
import com.orbit.commandcenter.domain.model.OrbitModule

/**
 * Immutable state rendered by [DashboardScreen].
 *
 * Marked [Immutable] even though its list is typed as `kotlin.collections.List` (which the
 * Compose compiler treats as unstable, as any implementation could be mutable). The promise
 * holds because the data and domain layers only ever hand out read-only snapshots that are
 * never modified after creation. Declaring it here keeps the Compose runtime out of the
 * domain layer while allowing the dashboard composables to be skipped.
 */
@Immutable
data class DashboardUiState(
    val modules: List<OrbitModule> = emptyList(),
    val isLoaded: Boolean = false,
) {
    val hasModules: Boolean
        get() = modules.isNotEmpty()
}
