package com.orbit.commandcenter.domain.model

/**
 * Lifecycle state of a product module.
 *
 * Phase 0 only surfaces the roadmap, so every module is [PLANNED]. The enum keeps the
 * UI honest: a module is never presented as usable before it actually exists.
 */
enum class ModuleStatus {
    PLANNED,
    IN_PROGRESS,
    AVAILABLE,
}

/**
 * A module of the ORBIT product (tasks, notes, projects, focus, ...).
 *
 * A pure domain type: no Android or Compose imports, so it is trivially unit-testable.
 */
data class OrbitModule(
    val id: String,
    val title: String,
    val description: String,
    val status: ModuleStatus,
)
