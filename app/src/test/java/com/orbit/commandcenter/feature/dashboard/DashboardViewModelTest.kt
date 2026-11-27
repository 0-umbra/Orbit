package com.orbit.commandcenter.feature.dashboard

import com.orbit.commandcenter.domain.model.ModuleStatus
import com.orbit.commandcenter.domain.model.OrbitModule
import com.orbit.commandcenter.testing.FakeOrbitModuleRepository
import com.orbit.commandcenter.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val modules = listOf(
        OrbitModule("tasks", "Tasks", "Capture everyday work.", ModuleStatus.PLANNED),
        OrbitModule("notes", "Notes", "Write things down.", ModuleStatus.PLANNED),
    )

    @Test
    fun initialState_isEmptyAndNotLoaded() {
        val viewModel = DashboardViewModel(FakeOrbitModuleRepository(modules))

        assertEquals(DashboardUiState(), viewModel.uiState.value)
        assertFalse(viewModel.uiState.value.isLoaded)
        assertFalse(viewModel.uiState.value.hasModules)
    }

    @Test
    fun uiState_exposesModulesFromRepositoryOnceCollected() = runTest {
        val viewModel = DashboardViewModel(FakeOrbitModuleRepository(modules))
        val scheduler = mainDispatcherRule.testDispatcher.scheduler

        val collectJob = launch(UnconfinedTestDispatcher(scheduler)) {
            viewModel.uiState.collect { }
        }
        scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("state should be loaded after the repository emits", state.isLoaded)
        assertTrue("state should contain modules", state.hasModules)
        assertEquals(modules, state.modules)

        collectJob.cancel()
    }
}
