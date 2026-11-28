package com.orbit.commandcenter.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.orbit.commandcenter.OrbitApplication
import com.orbit.commandcenter.domain.repository.OrbitModuleRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel for the dashboard.
 *
 * Holds no Android UI types: it maps the repository stream into [DashboardUiState] and
 * exposes it as a [StateFlow] so the screen survives configuration changes without
 * re-reading data.
 *
 * The mapping runs on [backgroundDispatcher] (`flowOn`) so that reading and projecting the
 * data is never done on the main thread — the frame thread only ever receives the finished
 * snapshot. Today the repository is in-memory, so this is a cheap hop; it is the seam the
 * Phase 1 database implementation needs.
 */
class DashboardViewModel(
    repository: OrbitModuleRepository,
    backgroundDispatcher: CoroutineDispatcher = Dispatchers.Default,
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = repository.observeModules()
        .map { modules -> DashboardUiState(modules = modules, isLoaded = true) }
        .flowOn(backgroundDispatcher)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
            initialValue = DashboardUiState(),
        )

    companion object {
        /** Builds the ViewModel with the process-wide [OrbitApplication] container. */
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = checkNotNull(
                    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY],
                ) as OrbitApplication
                DashboardViewModel(
                    repository = application.container.orbitModuleRepository,
                    backgroundDispatcher = application.container.backgroundDispatcher,
                )
            }
        }
    }
}
