plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.room) apply false
    alias(libs.plugins.roborazzi) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.ktlint) apply false
    // Puts the build-logic convention plugins on the root classpath for `subprojects {}` below.
    id("starter.quality") apply false
}

// The single gate that scripts/verify.py, CI and AGENTS.md call.
val verifyStarter by tasks.registering {
    group = "verification"
    description = "Builds, tests, lints and renders screenshots for every module."
}

val androidGateTasks = setOf(
    "assembleDebug",
    "assembleRelease",
    "testDebugUnitTest",
    "assembleDebugAndroidTest",
    "recordRoborazziDebug",
)
val jvmGateTasks = setOf("test")
val qualityGateTasks = setOf("detekt", "ktlintCheck")

subprojects {
    // Intermediate projects such as :core have no build file and nothing to check.
    if (!buildFile.exists()) return@subprojects
    apply(plugin = "starter.quality")

    // Matched by name when the task graph is built, so a module that lacks a task
    // (no androidTest sources, no screenshot plugin) is simply skipped.
    val module = this
    verifyStarter.configure {
        dependsOn(
            module.tasks.matching { task ->
                val isAndroid = module.pluginManager.hasPlugin("com.android.base")
                task.name in qualityGateTasks ||
                    (isAndroid && task.name in androidGateTasks) ||
                    (!isAndroid && task.name in jvmGateTasks)
            },
        )
    }
}
