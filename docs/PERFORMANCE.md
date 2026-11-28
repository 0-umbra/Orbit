# ORBIT Performance Guide

How to measure ORBIT on a real device, what CI verifies automatically, and which causes of
jank have actually been fixed versus only inspected.

> **Status statement.** No ORBIT build has been profiled on the Motorola Moto E13 by the
> project tooling. Everything in the "Measured" columns below comes from CI (compiler
> output, test results, build artefacts). The numbers that describe *frames* must be
> collected on your device with the procedure in this document.

## 1. Why the debug APK feels slow (measured cause, not a guess)

| Property | `debug` | `benchmark` (release-equivalent) |
| --- | --- | --- |
| `debuggable` | **yes** | no |
| R8 / code shrinking | **no** | yes |
| Resource shrinking | no | yes |
| AOT-compiled code at install | **no — interpreted/JIT only** | yes (with a baseline profile) |
| Compose tooling in APK | yes (`ui-tooling`) | no |
| Signing | debug key | debug key (for local install only) |

A `debuggable` app runs with the ART interpreter and JIT, never with AOT-compiled code, and
carries extra instrumentation. On entry-level hardware that alone is a large, per-frame
cost — and it is invisible in the UI, because the same Compose code is running. This is why
jitter appeared on *every* screen, including screens whose layout is completely static.

`app/build.gradle.kts` now provides a `benchmark` build type that is release-equivalent
(minified, shrunk, non-debuggable, `profileable`) but signed with the local debug key so it
can simply be installed. Use it for every performance statement you make about ORBIT.

## 2. Measure in five commands (PC + USB, free, no Android Studio)

```bash
# 1. Get an optimised, installable build (CI artifact "orbit-benchmark-apk", or build it):
./gradlew assembleBenchmark
adb install -r app/build/outputs/apk/benchmark/app-benchmark.apk

# 2. Run the scripted scroll measurement:
tools/perf/measure-scroll.sh

# 3. Compare with the debug build using the exact same gesture pattern:
./gradlew installDebug
tools/perf/measure-scroll.sh
```

The script resets `gfxinfo`, performs one warm-up scroll, then `SWIPES` (default 8)
gestures, and prints:

```
frames rendered  : 480
janky frames     : 37 (7.71%)
50th percentile  : 9ms
90th percentile  : 16ms
95th percentile  : 22ms
99th percentile  : 41ms
verdict          : mild jank (above the 5% bar) (7.71% janky)
```

Options (environment variables):

| Variable | Default | Meaning |
| --- | --- | --- |
| `PACKAGE` | `com.orbit.commandcenter` | App to measure. |
| `SWIPES` | `8` | Number of scroll gestures. Use the same value for every comparison. |
| `COMPILE=1` | `0` | AOT-compiles the installed build (`cmd package compile -m speed -f`) first. Run with and without it: the difference is the interpreter/JIT share of the jank, i.e. what a baseline profile would remove. |
| `OUT_DIR` | `build/perf` | Where the raw `dumpsys gfxinfo` dump is written (git-ignored). |
| `GFXINFO_FILE` | – | Parse a saved dump instead of driving a device (e.g. `GFXINFO_FILE=build/perf/gfxinfo-…txt tools/perf/measure-scroll.sh`). |

Reading the results (60 Hz device, 16.7 ms per frame):

- **janky frames ≤ 5 %** — acceptable for a released app.
- **95th percentile ≤ 16.7 ms** — nine frames in ten fit the frame budget.
- Compare *the same build type across runs*, not debug against benchmark, when judging a
  code change. Debug vs benchmark is only useful to see the cost of the build type.

A raw dump can also be inspected by hand:

```bash
adb shell dumpsys gfxinfo com.orbit.commandcenter framestats   # per-frame timings
adb shell dumpsys gfxinfo com.orbit.commandcenter reset
```

## 3. Measure without a PC (phone only)

Android's own tooling is enough for a qualitative comparison:

1. Settings → System → Developer options → **Profile HWUI rendering** → *On screen as bars*.
   Green bars are frames under budget, yellow/red are frames that missed vsync.
2. Also enable **Show refresh rate** to see the display flipping between 60 Hz and lower
   rates.
3. Scroll the dashboard for ~10 seconds, then the About screen, and count yellow/red bars.
   Do the same on the debug build and on the `benchmark` build and compare.

This gives no percentiles, but it is enough to confirm whether the build type is the
dominant factor on your device.

## 4. What CI verifies automatically (measured)

| Gate | Assertion |
| --- | --- |
| Unit tests | All JVM tests pass; zero tests executed is a failure. |
| Compose stability | `DashboardUiState` and `OrbitModule` are reported `stable`; a missing report fails the build. |
| Compose skippability | `DashboardScreen`, `DashboardContent`, `WelcomeCard`, `OrbitModuleCard`, `AboutScreen`, `StatusPill`, `OrbitBrandHeader`, `OrbitInfoCard`, `OrbitMark` are all `skippable`. |
| R8 / shrinking | `assembleBenchmark` succeeds **and** the benchmark APK is smaller than the debug APK. |
| Profileable | The merged benchmark manifest contains `<profileable>`, so traces can be captured. |
| Baseline profiles | Presence of `dexopt/baseline.prof` in the benchmark APK is reported. |
| Identity | Both APKs: `com.orbit.commandcenter`, minSdk 26, targetSdk 35, no Android system permission. |

Artefacts per run: `orbit-debug-apk`, `orbit-benchmark-apk`, `compose-compiler-reports`,
`unit-test-report`, `gradle-logs`.

## 5. Findings and their status

| # | Finding | Evidence | Status |
| --- | --- | --- | --- |
| 1 | `NavHost` used the library default transitions: a **700 ms** full-screen fade in *and* out on every navigation (`DefaultNavTransitions` in `androidx.navigation.compose`) — two screens composited and alpha-animated for ~42 frames at 60 Hz | Verified in the AndroidX source of `navigation-compose` | **Fixed** — explicit 180 ms fades in `OrbitNavHost.kt` |
| 2 | Debug builds are not representative: `debuggable` (no AOT), unminified, Compose tooling included | Build configuration; ART `debuggable` behaviour | **Fixed** — `release` now minified + shrunk; new installable `benchmark` variant for measurement |
| 3 | `DashboardUiState` was unstable because of `List<OrbitModule>` | Compose compiler report (Phase 0.1) and now CI-gated | Fixed in Phase 0.1 |
| 4 | Per-composition allocations (upper-cased pill labels, `BorderStroke`, `PaddingValues`, `Arrangement`) and per-frame draw work in the brand mark | Code inspection + `drawWithCache` | Fixed in Phase 0.1 |
| 5 | 1dp card borders could allocate a `Path` per card per frame | **Verified not a cause**: `Modifier.border` caches its `Path` in a `DrawWithCacheModifierNode` and draws rounded rectangles without rebuilding geometry | No change needed |
| 6 | Shadows, blurs, gradients or animated effects on scrolling content | Full source scan | **Verified absent** — surfaces are flat (0dp elevation) |
| 7 | `LazyColumn` missing keys / content types | Source | Already correct (stable keys + content types since Phase 0.1) |
| 8 | Work on the main thread (I/O, parsing) | Source: in-memory repository, mapping on an injected background dispatcher | **Verified none**. Note: the About screen's text layout happens during composition *once*, not per frame |
| 9 | Theme / navigation causing recreation of state | `collectAsStateWithLifecycle` + `StateFlow`; `rememberNavController`; no `LaunchedEffect` reloads | **Verified none**; state survives configuration changes |
| 10 | No baseline profile for ORBIT's own code, so first-run scrolling is JIT-warm-up bound | Phase 0 has no profile generation step | **Open** — see §6 |
| 11 | Screen content itself (Compose work per row) | Compose compiler reports + skippability | Verified minimal; no further indexable cause found |

Everything in rows 5–9 and 11 is a *rejected* hypothesis. They are recorded so future work
does not re-litigate them.

## 6. Open items and how to close them

1. **Baseline profile for ORBIT's own code.** Compose/AndroidX library profiles are merged
   into the APK automatically (CI reports whether `dexopt/baseline.prof` is present), but
   ORBIT's own Compose screens have no profile, so the first scroll of each session still
   pays JIT warm-up. Generating a profile needs a device or emulator:
   ```bash
   # once a :baselineprofile module exists
   ./gradlew :app:generateBaselineProfile     # needs a connected device
   ```
   Until then, `COMPILE=1 tools/perf/measure-scroll.sh` shows the size of the win on your
   device (`cmd package compile -m speed -f` emulates what installing with a profile does).

2. **Macrobenchmark module (FrameTimingMetric).** Deliberately **not** added yet: a
   `com.android.test` module can only produce numbers on a connected device, it cannot be
   validated in this repository's CI (no emulator), and a benchmark module that has never
   run is unreliable infrastructure. The adb script in `tools/perf/` provides the same
   frame-timing data today. When a device is available for CI or local runs, the standard
   setup is: `benchmark` build type (already present) + `:benchmark` module using
   `androidx.benchmark:benchmark-macro-junit4`.

3. **Smoke-test the minified build once.** R8 is verified to build and shrink in CI, but
   runtime correctness of a minified build cannot be asserted without a device. Before
   trusting benchmark numbers, install `app-benchmark.apk` and confirm the dashboard, the
   About screen and navigation all behave normally. If something is stripped, add the
   specific keep rule to `app/proguard-rules.pro` rather than disabling minification.
