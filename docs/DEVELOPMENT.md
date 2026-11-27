# ORBIT Development Guide

Everything needed to build, test and contribute to ORBIT.

## 1. Environment

| Requirement | Version | Notes |
| --- | --- | --- |
| JDK | 17 | Gradle, AGP 8.7.3 and Kotlin 2.0.21 all require 17; later JDKs are not used. |
| Gradle | 8.9 | Provided by the wrapper — do not install Gradle manually. |
| Android SDK platform | 35 | `compileSdk` / `targetSdk`. |
| Android SDK build-tools | 35.0.0 | Installed by the SDK manager / AGP. |
| Android Studio | Ladybug (2024.2.1) or newer | Optional; command line is enough. |

Check the toolchain:

```bash
java -version          # -> openjdk 17.x
./gradlew --version    # -> Gradle 8.9, JVM 17
```

## 2. First checkout

```bash
git clone https://github.com/0-umbra/Orbit.git
cd Orbit
git checkout develop

# Point Gradle at your SDK if ANDROID_HOME is not set (never commit this file)
echo "sdk.dir=$HOME/Android/Sdk" > local.properties

./gradlew assembleDebug
```

The debug APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

## 3. Everyday commands

```bash
./gradlew assembleDebug          # Debug APK
./gradlew testDebugUnitTest      # JVM unit tests
./gradlew installDebug           # Install the debug build on a connected device
./gradlew clean                  # Clear build outputs
./gradlew build                  # Full build (APKs + unit tests + lint)
```

Useful extras:

```bash
./gradlew tasks --group=verification   # Everything that verifies the project
./gradlew lintDebug                    # Android lint for the debug variant
```

Version bumps are made in `gradle/libs.versions.toml` only; module build files reference
the catalog aliases.

## 4. Testing

Unit tests live in `app/src/test/java/...` and run on the JVM — no emulator needed.

```bash
./gradlew testDebugUnitTest
open app/build/reports/tests/testDebugUnitTest/index.html
```

Conventions:

- Test names describe behaviour: `uiState_exposesModulesFromRepositoryOnceCollected`.
- ViewModels are tested with a hand-written fake repository plus `MainDispatcherRule`
  (which swaps `Dispatchers.Main` for a `TestDispatcher`).
- Domain models are tested without Android at all.
- Compose UI tests will be added to `app/src/androidTest` with the first interactive
  feature.

## 5. Continuous integration

`.github/workflows/android-ci.yml` runs on:

- every pull request whose base branch is `develop`,
- every push to a `feature/**` branch,
- manual dispatch.

The job sets up JDK 17 (Temurin), configures Gradle caching via
`gradle/actions/setup-gradle`, runs `testDebugUnitTest`, builds `assembleDebug`, checks the
APK exists, and uploads two artifacts: `unit-test-report` and `orbit-debug-apk` (14-day
retention). Workflow permissions are `contents: read`; no secrets are used.

## 6. Git branching policy

| Branch | Purpose | Rules |
| --- | --- | --- |
| `main` | Released, stable builds | Protected. Only updated by reviewed merges from `develop`. |
| `develop` | Integration | Default target for pull requests. Must stay green. |
| `feature/<slug>` | A single task | Branched from `develop`, rebased/merged via pull request, deleted after merge. |

Workflow:

```bash
git checkout develop
git pull --ff-only
git checkout -b feature/phase-0-android-foundation
# ... work ...
./gradlew testDebugUnitTest assembleDebug
git add -A
git commit -m "app: add Phase 0 Android foundation"
git push -u origin feature/phase-0-android-foundation
gh pr create --base develop --title "Phase 0: Android foundation" --body "..."
```

Rules:

1. Never commit or push directly to `main` or `develop`.
2. Never force-push a shared branch.
3. One pull request per task, always targeting `develop`.
4. A pull request is merged only with a green CI run and review approval.
5. Commit messages: `area: imperative summary` (for example
   `theme: add light colour scheme`).
6. Do not commit keystores, `local.properties`, build outputs or secrets.

## 7. Manual device verification checklist

Physical testing is done by the project owner; CI cannot cover it.

Target device: **Motorola Moto E13, Android 13, 8 GB RAM**.

1. `./gradlew installDebug` (or sideload `app/build/outputs/apk/debug/app-debug.apk`).
2. Launcher shows the ORBIT icon and label; the themed (monochrome) icon is correct on
   Android 13.
3. Cold start shows the graphite background with no white flash, then the dashboard.
4. Dashboard renders: brand mark, welcome card, seven roadmap cards, privacy note.
5. "About ORBIT" opens the about screen; "Back" (and system back) returns to the dashboard.
6. Rotate the device: state is preserved, no layout clipping at 360 dp width.
7. Toggle system dark mode: both themes remain legible.
8. Increase system font size to the largest setting and confirm no truncated text.
9. Enable TalkBack: the mark and headings are announced in a sensible order.
10. Check `adb shell dumpsys package com.orbit.commandcenter | grep -i permission` — the
    app requests no runtime permissions.

## 8. Troubleshooting

| Symptom | Fix |
| --- | --- |
| `SDK location not found` | Create `local.properties` with `sdk.dir=...`, or export `ANDROID_HOME`. |
| `Unsupported class file major version` | You are on the wrong JDK. Run with JDK 17. |
| `Failed to install... SDK platform 35` | Install platform 35 and build-tools 35.0.0 in the SDK manager and accept the licenses. |
| Compose compiler errors | Kotlin and the Compose compiler plugin are versioned together (`kotlin` in the catalog) — do not pin them separately. |
| CI fails only on `setup-gradle` wrapper validation | The wrapper jar must be regenerated with `./gradlew wrapper --gradle-version 8.9`, not replaced by hand. |

## 9. Phase status

Phase 0 delivers the foundation only: build setup, theme, navigation, MVVM plumbing, one
launch screen, CI and documentation. Tasks, notes, projects, goals, focus, reminders and
insights are on the roadmap (`OrbitRoadmap`) and are not implemented.
