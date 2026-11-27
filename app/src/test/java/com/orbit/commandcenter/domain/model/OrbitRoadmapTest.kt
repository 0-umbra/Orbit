package com.orbit.commandcenter.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OrbitRoadmapTest {

    @Test
    fun roadmap_isNotEmpty() {
        assertTrue(OrbitRoadmap.modules.isNotEmpty())
    }

    @Test
    fun roadmap_idsAreUnique() {
        val ids = OrbitRoadmap.modules.map { module -> module.id }
        assertEquals("module ids must be unique", ids.size, ids.distinct().size)
    }

    @Test
    fun roadmap_entriesAreWellFormed() {
        val wellFormed = OrbitRoadmap.modules.all { module ->
            module.id.isNotBlank() && module.title.isNotBlank() && module.description.isNotBlank()
        }
        assertTrue("every module needs an id, title and description", wellFormed)
    }

    @Test
    fun roadmap_shipsNoAvailableModulesInPhaseZero() {
        assertEquals(0, OrbitRoadmap.countWithStatus(ModuleStatus.AVAILABLE))
        assertTrue(OrbitRoadmap.modulesWithStatus(ModuleStatus.AVAILABLE).isEmpty())
    }

    @Test
    fun modulesWithStatus_andCountWithStatus_agree() {
        ModuleStatus.entries.forEach { status ->
            assertEquals(
                OrbitRoadmap.modulesWithStatus(status).size,
                OrbitRoadmap.countWithStatus(status),
            )
        }
    }
}
