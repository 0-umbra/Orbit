# ORBIT ProGuard rules.
#
# Release builds are currently not minified (isMinifyEnabled = false), so these
# rules are a placeholder for the Phase 1 release configuration. Compose and
# AndroidX ship their own consumer rules; add app-specific keep rules here only
# when obfuscation is enabled.

# Keep line numbers so that future crash reports remain readable.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
