plugins {
    id("starter.jvm.library")
}

dependencies {
    testImplementation(libs.konsist)
}

// Konsist reads source files directly, which Gradle does not see. Declare them as inputs so
// :konsist:test re-runs (and misses the build cache) whenever any project source changes.
tasks.withType<Test>().configureEach {
    val root = rootProject.layout.projectDirectory
    inputs
        .files(
            root.asFileTree.matching {
                include("**/src/**/*.kt", "**/*.kts")
                // Module build directories only (root, app, feature/x, core/testing/x, build-logic/x).
                // A `**/build/**` pattern would also drop source packages such as com/build/.
                exclude("build/**", "*/build/**", "*/*/build/**", "*/*/*/build/**")
                exclude("**/.gradle/**", "**/.kotlin/**")
            },
        ).withPathSensitivity(PathSensitivity.RELATIVE)
        .withPropertyName("projectSources")
}
