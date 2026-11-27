package com.orbit.commandcenter.domain.model

/**
 * The ORBIT product roadmap as data.
 *
 * This is the single source of truth for the module list shown on the dashboard, so the
 * UI can never drift from what the project actually plans to ship. Modules move out of
 * this catalogue and into their own feature package as they are implemented.
 */
object OrbitRoadmap {

    val modules: List<OrbitModule> = listOf(
        OrbitModule(
            id = "tasks",
            title = "Tasks",
            description = "Capture, schedule and complete everyday work without friction.",
            status = ModuleStatus.PLANNED,
        ),
        OrbitModule(
            id = "projects",
            title = "Projects",
            description = "Group related work and see progress at a glance.",
            status = ModuleStatus.PLANNED,
        ),
        OrbitModule(
            id = "notes",
            title = "Notes",
            description = "A quiet place for notebooks, ideas and reference material.",
            status = ModuleStatus.PLANNED,
        ),
        OrbitModule(
            id = "goals",
            title = "Goals and habits",
            description = "Keep long-term goals and daily routines visible.",
            status = ModuleStatus.PLANNED,
        ),
        OrbitModule(
            id = "focus",
            title = "Focus timer",
            description = "Protect deep work with a deliberately simple timer.",
            status = ModuleStatus.PLANNED,
        ),
        OrbitModule(
            id = "reminders",
            title = "Reminders",
            description = "Gentle nudges that fire on time, on the device.",
            status = ModuleStatus.PLANNED,
        ),
        OrbitModule(
            id = "insights",
            title = "Insights",
            description = "Understand where your attention actually goes.",
            status = ModuleStatus.PLANNED,
        ),
    )

    /** Modules currently in the given [status], in catalogue order. */
    fun modulesWithStatus(status: ModuleStatus): List<OrbitModule> =
        modules.filter { module -> module.status == status }

    /** How many modules are in the given [status]. */
    fun countWithStatus(status: ModuleStatus): Int =
        modules.count { module -> module.status == status }
}
