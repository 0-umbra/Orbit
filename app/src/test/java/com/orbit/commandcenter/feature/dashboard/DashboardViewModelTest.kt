package com.orbit.commandcenter.feature.dashboard

import com.orbit.commandcenter.domain.model.ModuleStatus
import com.orbit.commandcenter.domain.model.OrbitModule
import com.orbit.commandcenter.testing.FakeOrbitModuleRepository
import com.orbit.commandcenter.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
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

    private fun testScheduler(): TestCoroutineScheduler =
        mainDispatcherRule.testDispatcher.scheduler

    private fun dashboardViewModel(
        repository: FakeOrbitModuleRepository = FakeOrbitModuleRepository(modules),
    ): DashboardViewModel = DashboardViewModel(
        repository = repository,
        backgroundDispatcher = UnconfinedTestDispatcher(testScheduler()),
    )

    @Test
    fun initialState_isEmptyAndNotLoaded() {
        val viewModel = dashboardViewModel()

        assertEquals(DashboardUiState(), viewModel.uiState.value)
        assertFalse(viewModel.uiState.value.isLoaded)
        assertFalse(viewModel.uiState.value.hasModules)
    }

    @Test
    fun uiState_exposesModulesFromRepositoryOnceCollected() = runTest {
        val viewModel = dashboardViewModel()
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler())) {
            viewModel.uiState.collect { }
        }
        testScheduler().advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("state should be loaded after the repository emits", state.isLoaded)
        assertTrue("state should contain modules", state.hasModules)
        assertEquals(modules, state.modules)

        collectJob.cancel()
    }

    /**
     * Compose can only skip a composable when the state object it receives is equal (or
     * identical) to the previous one. Re-projecting the module list into a new instance on
     * every emission would silently force the whole dashboard — including visible list
     * rows — to recompose while scrolling.
     */
    @Test
    fun uiState_reusesTheRepositoryListInstance() = runTest {
        val viewModel = dashboardViewModel()
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler())) {
            viewModel.uiState.collect { }
        }
        testScheduler().advanceUntilIdle()

        assertSame(
            "the dashboard state must reuse the repository snapshot instead of copying it",
            modules,
            viewModel.uiState.value.modules,
        )

        collectJob.cancel()
    }
}
