package com.orbit.commandcenter.domain.repository

import com.orbit.commandcenter.domain.model.OrbitModule
import kotlinx.coroutines.flow.Flow

/**
 * Read access to the ORBIT module catalogue.
 *
 * The UI depends on this interface only. Phase 0 serves the catalogue from memory; the
 * Phase 1 implementation will read persisted state from the local database, which is why
 * the contract already exposes a [Flow] instead of a plain list.
 */
interface OrbitModuleRepository {
    fun observeModules(): Flow<List<OrbitModule>>
}
