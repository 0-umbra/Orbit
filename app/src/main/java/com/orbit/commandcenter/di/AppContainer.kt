package com.orbit.commandcenter.di

import com.orbit.commandcenter.data.repository.InMemoryOrbitModuleRepository
import com.orbit.commandcenter.domain.repository.OrbitModuleRepository

/**
 * Dependency container for the application.
 *
 * Phase 0 only needs one repository; new dependencies are added here as features land.
 * The interface exists so that tests and future build variants can substitute
 * implementations without touching the UI layer.
 */
interface AppContainer {
    val orbitModuleRepository: OrbitModuleRepository
}

/**
 * Default container wired for the single `app` module of the Phase 0 foundation.
 */
class DefaultAppContainer : AppContainer {

    override val orbitModuleRepository: OrbitModuleRepository by lazy {
        InMemoryOrbitModuleRepository()
    }
}
