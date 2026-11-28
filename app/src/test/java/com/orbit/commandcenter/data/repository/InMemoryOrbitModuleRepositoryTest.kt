package com.orbit.commandcenter.data.repository

import com.orbit.commandcenter.domain.model.ModuleStatus
import com.orbit.commandcenter.domain.model.OrbitModule
import com.orbit.commandcenter.domain.model.OrbitRoadmap
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class InMemoryOrbitModuleRepositoryTest {

    @Test
    fun observeModules_emitsTheConfiguredModules() = runTest {
        val modules = listOf(
            OrbitModule("tasks", "Tasks", "Capture everyday work.", ModuleStatus.PLANNED),
        )
        val repository = InMemoryOrbitModuleRepository(modules)

        assertEquals(modules, repository.observeModules().first())
    }

    @Test
    fun observeModules_defaultsToTheRoadmap() = runTest {
        val repository = InMemoryOrbitModuleRepository()

        assertEquals(OrbitRoadmap.modules, repository.observeModules().first())
        assertEquals(OrbitRoadmap.modules.size, repository.observeModules().first().size)
    }

    /**
     * The snapshot is taken once at construction. Re-collecting must yield the same list
     * instance, not a copy: an unstable list identity is what breaks Compose skipping in
     * the dashboard.
     */
    @Test
    fun observeModules_reusesTheSameSnapshotAcrossCollections() = runTest {
        val repository = InMemoryOrbitModuleRepository()

        val first = repository.observeModules().first()
        val second = repository.observeModules().first()

        assertSame(first, second)
    }
}
