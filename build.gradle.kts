// ORBIT — root Gradle build script.
// Plugins are declared here (without applying them) so that versions are resolved
// once and shared by every module. See gradle/libs.versions.toml for the versions.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}
