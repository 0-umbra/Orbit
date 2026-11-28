plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.orbit.commandcenter"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.orbit.commandcenter"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        debug {
            // The debug variant intentionally keeps the production application id so that
            // the built APK can be verified against `com.orbit.commandcenter`.
            // Debug builds are NOT representative of real performance: they are not
            // minified, they run as `debuggable` (no AOT-compiled code, JIT only) and they
            // include Compose tooling. Measure with the `benchmark` build type instead.
            isMinifyEnabled = false
        }
        release {
            // R8 + resource shrinking. This is what makes a release build fast enough to be
            // worth comparing against debug on entry-level hardware.
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
        /**
         * Release-equivalent build used to measure real performance on a physical device:
         * minified and shrunk like `release`, not debuggable, `profileable` (see
         * app/src/benchmark/AndroidManifest.xml) and signed with the local debug key so that
         * `./gradlew installBenchmark` works without any signing setup.
         *
         * This variant is for measurement only — it is not a publishable artefact.
         */
        create("benchmark") {
            initWith(getByName("release"))
            signingConfig = signingConfigs.getByName("debug")
            isDebuggable = false
            versionNameSuffix = "-benchmark"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = false
        }
    }
}

/**
 * Compose compiler reports: `-classes.txt` lists the inferred stability of every class and
 * `-composables.txt` lists whether each composable is restartable/skippable. CI asserts the
 * dashboard types stay stable and skippable, which is what keeps scrolling allocation-free.
 */
composeCompiler {
    reportsDestination.set(layout.buildDirectory.dir("compose_reports"))
    metricsDestination.set(layout.buildDirectory.dir("compose_metrics"))
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    debugImplementation(libs.androidx.compose.ui.tooling)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
