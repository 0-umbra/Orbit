package com.orbit.commandcenter.testing

import com.orbit.commandcenter.domain.model.OrbitModule
import com.orbit.commandcenter.domain.repository.OrbitModuleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Test double that emits a fixed module list.
 */
class FakeOrbitModuleRepository(
    private val modules: List<OrbitModule>,
) : OrbitModuleRepository {

    override fun observeModules(): Flow<List<OrbitModule>> = flowOf(modules)
}
