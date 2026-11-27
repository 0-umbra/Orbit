package com.orbit.commandcenter.data.repository

import com.orbit.commandcenter.domain.model.OrbitModule
import com.orbit.commandcenter.domain.model.OrbitRoadmap
import com.orbit.commandcenter.domain.repository.OrbitModuleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Phase 0 implementation of [OrbitModuleRepository].
 *
 * The catalogue is static, so it is served from memory. Phase 1 replaces this class with
 * a database-backed implementation behind the same interface; nothing above the data
 * layer has to change.
 */
class InMemoryOrbitModuleRepository(
    private val modules: List<OrbitModule> = OrbitRoadmap.modules,
) : OrbitModuleRepository {

    private val snapshot: List<OrbitModule> = modules.toList()

    override fun observeModules(): Flow<List<OrbitModule>> = flowOf(snapshot)
}
