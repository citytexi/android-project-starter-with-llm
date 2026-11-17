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
    // Applied here too so ktlint checks the root build.gradle.kts and settings.gradle.kts
    // (ktlintKotlinScriptCheck). Modules get ktlint from starter.quality below.
    alias(libs.plugins.ktlint)
    // Puts the build-logic convention plugins on the root classpath for `subprojects {}` below.
    id("starter.quality") apply false
}

// Fast checks for every module: unit tests, instrumented-test compile, Konsist, detekt and
// ktlint. No release build and no screenshot recording. The `test` CI workflow runs this.
val testStarter by tasks.registering {
    group = "verification"
    description = "Runs unit tests, compiles instrumented tests and lints every module."
    // ModuleRulesTest lives in the included build-logic build, which subprojects {} does not reach.
    dependsOn(gradle.includedBuild("build-logic").task(":convention:test"))
    dependsOn(tasks.named("ktlintCheck"))
}

// The single gate that scripts/verify.py and AGENTS.md call: testStarter plus builds and screenshots.
val verifyStarter by tasks.registering {
    group = "verification"
    description = "Builds, tests, lints and renders screenshots for every module."
    dependsOn(testStarter)
}

val androidTestTasks = setOf("testDebugUnitTest", "assembleDebugAndroidTest")
val androidBuildTasks = setOf("assembleDebug", "assembleRelease", "recordRoborazziDebug")
val jvmTestTasks = setOf("test")
val qualityTasks = setOf("detekt", "ktlintCheck")

subprojects {
    // Intermediate projects such as :core have no build file and nothing to check.
    if (!buildFile.exists()) return@subprojects
    apply(plugin = "starter.quality")

    // Matched by name when the task graph is built, so a module that lacks a task
    // (no androidTest sources, no screenshot plugin) is simply skipped.
    val module = this

    fun matching(
        android: Set<String>,
        jvm: Set<String>,
        any: Set<String>,
    ) = module.tasks.matching { task ->
        val isAndroid = module.pluginManager.hasPlugin("com.android.base")
        task.name in any || task.name in (if (isAndroid) android else jvm)
    }
    testStarter.configure {
        dependsOn(matching(android = androidTestTasks, jvm = jvmTestTasks, any = qualityTasks))
    }
    verifyStarter.configure {
        dependsOn(matching(android = androidBuildTasks, jvm = emptySet(), any = emptySet()))
    }
}
