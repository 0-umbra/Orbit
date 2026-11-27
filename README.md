# ORBIT

A premium, offline-first personal command center for Android.

ORBIT brings tasks, projects, notes, goals, habits, a focus timer, reminders and
productivity insights into one calm application. It is designed to be private by
default: everything stays on the device, and the Phase 0 build requests no Android
system permission at all.

> **Status: Phase 0 — Engineering foundation.**
> This repository currently ships the native Android foundation (build setup, theme,
> navigation, MVVM plumbing, CI and documentation) plus one polished launch screen.
> No productivity feature is implemented yet, and the roadmap cards on the dashboard
> are intentionally labelled *Planned*.

## Technology stack

| Area | Choice |
| --- | --- |
| Language | Kotlin 2.0.21 |
| UI | Jetpack Compose (BOM 2024.10.01), Material 3 |
| Architecture | MVVM with unidirectional data flow (`StateFlow`), manual dependency container |
| Build | Gradle 8.9, Android Gradle Plugin 8.7.3, Gradle Kotlin DSL, version catalog |
| Toolchain | JDK 17 |
| SDK | `minSdk` 26, `compileSdk` 35, `targetSdk` 35 |
| Tests | JUnit 4 + `kotlinx-coroutines-test` (JVM unit tests) |
| CI | GitHub Actions (unit tests, debug APK, artifact upload) |

Application ID: `com.orbit.commandcenter`

## Project structure

```
Orbit/
├── .github/workflows/android-ci.yml    # CI: unit tests + debug APK artifact
├── app/
│   ├── build.gradle.kts                # App module configuration
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/com/orbit/commandcenter/
│       │   │   ├── OrbitApplication.kt     # Entry point + dependency container owner
│       │   │   ├── MainActivity.kt         # Single Compose-host activity
│       │   │   ├── di/                     # Manual dependency container
│       │   │   ├── domain/                 # Pure Kotlin models + repository contracts
│       │   │   ├── data/                   # Repository implementations
│       │   │   ├── feature/                # Feature packages (dashboard, about)
│       │   │   ├── navigation/             # Destinations + Compose NavHost
│       │   │   └── ui/                     # Theme + shared components
│       │   └── res/                        # Strings, theme XML, adaptive launcher icon
│       └── test/java/com/orbit/commandcenter/   # JVM unit tests
├── docs/ARCHITECTURE.md
├── docs/DEVELOPMENT.md
├── gradle/libs.versions.toml           # Single source of dependency versions
└── settings.gradle.kts
```

A single `:app` module is used on purpose: at this size, feature packages inside the
module give the same separation of concerns as Gradle modules without the build cost.
The package boundaries are already the ones a multi-module split would use.

## Build

Requirements: JDK 17 and the Android SDK with platform 35 and build-tools 35.x.

```bash
# Debug APK -> app/build/outputs/apk/debug/app-debug.apk
./gradlew assembleDebug

# Unit tests
./gradlew testDebugUnitTest

# Install on a connected device
./gradlew installDebug
```

If the SDK is not found automatically, create `local.properties` with
`sdk.dir=/path/to/Android/sdk` (never commit that file).

## Testing

```bash
./gradlew testDebugUnitTest
```

The HTML report is written to `app/build/reports/tests/testDebugUnitTest/index.html`.
CI runs the same task and uploads the report and the debug APK for every pull request
targeting `develop`.

## Git branching policy

- `main` — stable, user-visible releases. Never commit to it directly.
- `develop` — integration branch. All pull requests target `develop`.
- `feature/*` — one branch per task (for example `feature/phase-0-android-foundation`),
  branched from `develop`, merged back through a pull request.
- Commits are short, imperative and scoped (`app: add dashboard state holder`).
- No force-pushes, no direct pushes to `main` or `develop`, and no pull request is
  merged without review and a green CI run.

## Device targets

- `minSdk` 26 (Android 8.0) — covers ordinary and low-cost devices.
- Primary test device for this project: Motorola Moto E13, Android 13, 8 GB RAM.
- No Android system permissions, no background services and no network dependency in
  Phase 0. The merged manifest contains one app-private signature permission
  (`com.orbit.commandcenter.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION`) that
  `androidx.core` generates automatically to guard dynamically registered receivers; it is
  not requested from the user and CI asserts that nothing else is declared.

Testing on physical hardware has **not** been performed as part of this phase; see
`docs/DEVELOPMENT.md` for the manual verification checklist.

## Documentation

- [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) — layers, data flow, package rules, database plan.
- [docs/DEVELOPMENT.md](docs/DEVELOPMENT.md) — environment setup, build/test commands, CI, release notes.

## License

No license has been chosen yet. All rights reserved until a license is added.
