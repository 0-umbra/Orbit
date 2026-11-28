# ORBIT Architecture

Phase 0 establishes the skeleton that later phases fill in. The goal is a structure that
is obvious to read, cheap to build and easy to extend — not a framework.

## Design principles

1. **Offline-first.** The device is the source of truth. No feature may require a network
   call to be useful. Phase 0 has no network code and requests no permissions.
2. **Unidirectional data flow.** UI renders immutable state; events go up to a ViewModel;
   data comes from a repository as a stream.
3. **One dependency direction.** `feature → domain ← data`. The domain layer is pure
   Kotlin and knows nothing about Android, Compose, Room or networking.
4. **No unnecessary abstraction.** One module, a hand-written dependency container, and
   no DI/animation/database library until a feature actually needs it.
5. **Small and reliable over broad and fragile.** Every dependency in
   `gradle/libs.versions.toml` is used by shipping code or tests.

## Layers

```
┌──────────────────────────────────────────────────────────────┐
│ UI (Compose)            feature/dashboard, feature/about     │
│   stateless screens + ViewModels exposing StateFlow          │
├──────────────────────────────────────────────────────────────┤
│ Navigation              navigation/OrbitDestination, NavHost  │
├──────────────────────────────────────────────────────────────┤
│ Domain (pure Kotlin)    domain/model, domain/repository      │
├──────────────────────────────────────────────────────────────┤
│ Data                    data/repository                      │
├──────────────────────────────────────────────────────────────┤
│ Platform                AndroidManifest, resources, theme    │
└──────────────────────────────────────────────────────────────┘
```

### Application entry point

- `OrbitApplication` creates a single `AppContainer` and nothing else. It has no
  startup work, so cold start stays fast on low-end devices.
- `MainActivity` is the only activity. It calls `enableEdgeToEdge()`, applies
  `OrbitTheme`, and hosts `OrbitApp()` (the navigation graph).

### Dependency injection

`di/AppContainer` is a plain interface implemented by `DefaultAppContainer`.
ViewModels obtain dependencies through `viewModelFactory { initializer { ... } }` reading
`APPLICATION_KEY`. This keeps the build free of annotation processors (KSP/Hilt) while
keeping testability: a test constructs a ViewModel with a fake repository directly.

When the dependency graph grows beyond a handful of objects, this container can be
replaced by a DI framework without touching feature or domain code.

### UI and theme

- `ui/theme` holds the palette (`Color.kt`), typography (`Type.kt`), shapes (`Shape.kt`)
  and the `OrbitTheme` composable with a dark and a light `ColorScheme`.
- Dark is the canonical look: graphite background `#111318`, charcoal surfaces
  `#20232B`, violet primary `#A99AE8`, blue secondary `#83A6C9`, soft white text
  `#F0F0F2`. The light scheme deepens the accents so contrast stays AA.
- System dynamic colour is deliberately not used, so ORBIT looks the same on every device.
- `ui/components` holds shared, feature-agnostic composables: `OrbitMark` (the brand mark,
  drawn with Canvas so no raster assets are needed), `OrbitBrandHeader`, `StatusPill`,
  `OrbitInfoCard`. Feature screens compose these instead of re-inventing surfaces.

### Navigation

`navigation/OrbitDestination` declares every route; `navigation/OrbitNavHost.kt`
(`OrbitApp()`) maps them with Navigation Compose. Phase 0 has two destinations:
`orbit/dashboard` (start) and `orbit/about`. Adding a feature means adding one destination
and one `composable` block.

### Feature packages

Each feature is a self-contained package under `feature/<name>`:

```
feature/dashboard/
├── DashboardScreen.kt     # Composable screen; stateless content + stateful wrapper
├── DashboardViewModel.kt  # State holder, StateFlow<DashboardUiState>, factory
└── DashboardUiState.kt    # Immutable UI model
```

Rules for future features:

- The screen never talks to a repository directly.
- The ViewModel never imports Compose or `android.view`.
- UI state is immutable data; derived values live as `val` getters or computed properties.
- One-shot events (navigation, snackbars) are exposed as state or explicit callbacks, not
  as a shared event bus.

### Domain

- `domain/model/OrbitModule.kt` — a module of the product and its `ModuleStatus`
  (`PLANNED`, `IN_PROGRESS`, `AVAILABLE`).
- `domain/model/OrbitRoadmap.kt` — the module catalogue as data; the dashboard renders it,
  so the UI cannot claim a feature exists before it does.
- `domain/repository/OrbitModuleRepository.kt` — read contract returning
  `Flow<List<OrbitModule>>`. A stream (not a list) because the Phase 1 implementation reads
  from the database and must push updates.

### Data

- `data/repository/InMemoryOrbitModuleRepository.kt` serves the static catalogue.
- This is the only data source in Phase 0. It exists so the UI is already written against
  the interface that the database-backed repository will implement.

### Database layer (prepared, not implemented)

Phase 0 intentionally contains no database dependency: an empty schema would add an
annotation processor, a build plugin and a migration surface without storing anything.
The architecture is ready for it:

1. Add Room (with KSP) to `gradle/libs.versions.toml` and the `:app` module.
2. Create `data/local/` with `OrbitDatabase` (a `RoomDatabase`), entities, DAOs and
   converters. Everything user-authored is local-only.
3. Implement `OrbitModuleRepository` (and future repositories) on top of DAOs, exposing
   `Flow` queries that the existing ViewModels already consume.
4. Add `data/local/` migration tests and keep the database schema exported
   (`room.schemaLocation`) so migrations can be validated in CI.

Because the domain contract is already a `Flow`, no UI or ViewModel code has to change
when persistence arrives.

## Data flow (dashboard)

```
InMemoryOrbitModuleRepository.observeModules()  : Flow<List<OrbitModule>>
        │
        ▼  map
DashboardViewModel.uiState                     : StateFlow<DashboardUiState>
        │
        ▼  collectAsStateWithLifecycle()
DashboardScreen → DashboardContent (stateless) → LazyColumn of module cards
```

`stateIn(..., SharingStarted.WhileSubscribed(5_000), initialValue)` keeps collection alive
across short configuration changes and stops work when the screen goes away.

## Threading

- Repositories expose cold `Flow`s; the ViewModel maps them with `flowOn(backgroundDispatcher)`,
  where the dispatcher comes from `AppContainer`. The main thread only ever receives a
  finished snapshot.
- The dispatcher is injected rather than hard-coded, so tests substitute a deterministic
  test dispatcher and the Phase 1 database work inherits the same seam.
- Compose reads state on the main thread only; no synchronous I/O in composables.
- `Dispatchers.Default` is used because the current work is CPU-bound projection; database
  access added later should use `Dispatchers.IO` (or Room's own dispatcher).

## Performance

Scrolling is the interaction users feel most, and ORBIT targets entry-level hardware, so
the rules below are part of the architecture rather than an afterthought.

**Lists**

- Every dynamic collection is a `Lazy*` list. The dashboard is one `LazyColumn`; there are
  no nested scrollables.
- Items carry stable `key`s (the domain id, or a constant for the static rows) so items keep
  their identity and state across recomposition and reordering.
- Items carry `contentType`s so the lazy layout can reuse slot content for the uniform
  module cards.
- The only non-lazy scroller is the About screen: five short, static cards composed once are
  cheaper than lazy-list bookkeeping.

**Stability**

- `DashboardUiState` is `@Immutable`. Its `List<OrbitModule>` would otherwise make the whole
  state unstable (any `List` implementation could be mutable), which would prevent the
  dashboard composables from being skipped.
- `OrbitModule` is inferred stable by the compiler (a data class of `String`, enum and
  `Int`-like values); the domain layer therefore stays free of Compose annotations.
- Repositories hand out one immutable snapshot and reuse it: `InMemoryOrbitModuleRepository`
  copies once at construction and returns the same instance on every collection, so state
  objects stay identical and Compose can skip work. Unit tests pin this down.
- `derivedStateOf` is deliberately not used: no UI value is derived from frequently changing
  state (scroll position, text input). Adding it here would be ceremony, not optimisation.

**Composition hygiene**

- Composables only read state and build the tree; no parsing, sorting, formatting or
  upper-casing happens during composition.
- `StatusPill` remembers its upper-cased label and its alpha-modified colours/border per
  input instead of recomputing them for every card that scrolls into view.
- `BorderStroke` instances are hoisted through `rememberOrbitCardBorder()`.
- Static `PaddingValues`, `Arrangement` and `Modifier` chains are file-level values, so they
  are allocated once for the process rather than once per composition.
- `statusLabel`/`statusAccent` are `@ReadOnlyComposable`, so they add no group or
  recomposition scope to each row.

**Drawing**

- Surfaces are flat: `tonalElevation = 0.dp`, `shadowElevation = 0.dp`, 1dp hairline borders.
  There are no shadows, blurs, gradients or animated effects on scrolling content.
- The brand mark uses `drawWithCache`: the ring `Path`, the `Stroke` and the satellite
  geometry are built once per size, so a scroll frame replays three draw commands with no
  trigonometry and no allocations.
- Text uses the platform font (no downloadable fonts, no font assets) and the Material 3
  type scale.

**Regression guards**

- Unit tests assert that the repository and the ViewModel reuse list instances instead of
  copying them (copying would silently defeat skipping).
- CI generates the Compose compiler stability/skippability reports and fails if
  `DashboardUiState`/`OrbitModule` stop being stable or if a dashboard composable stops
  being skippable. The reports are uploaded as the `compose-compiler-reports` artifact.

## Accessibility and quality baseline

- All user-facing strings live in `res/values/strings.xml`.
- Text uses Material 3 type scale; no fixed pixel font sizes in screens.
- Accent colours are used for text/icons only where contrast is sufficient; body copy uses
  `onSurface` / `onSurfaceVariant`.
- Touch targets follow Material defaults (buttons, chips, cards).
- `supportsRtl="true"` with start/end padding throughout.
- No system permissions, no services, no `allowBackup` (personal data must not leave the
  device). The processed manifest still contains one app-private signature permission,
  `DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION`, injected by `androidx.core` for its
  dynamically registered receivers. It is never granted to the user and CI fails if any
  `android.permission.*` entry ever appears.

## Testing strategy

| Level | Scope | Location |
| --- | --- | --- |
| Unit (JVM) | Domain models, repository mapping, ViewModel state | `app/src/test/...` |
| Instrumented | Compose UI behaviour (added with the first interactive feature) | `app/src/androidTest/...` |
| CI | `testDebugUnitTest` + `assembleDebug` on every PR to `develop` | `.github/workflows/android-ci.yml` |

## Deliberate non-goals in Phase 0

AI features, cloud sync, authentication, analytics, advertising, Firebase, task/note
persistence and background services. Nothing in this phase may require a paid service or
a network permission.
