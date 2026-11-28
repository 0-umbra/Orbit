package com.orbit.commandcenter

import android.app.Application
import com.orbit.commandcenter.di.AppContainer
import com.orbit.commandcenter.di.DefaultAppContainer

/**
 * ORBIT application entry point.
 *
 * Owns the single [AppContainer] instance for the process. Keeping dependencies in a
 * plain container (instead of a DI framework) keeps Phase 0 small, fast and free of
 * extra build plugins; the container can be replaced by a DI framework later without
 * touching feature code.
 */
class OrbitApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer()
    }
}
