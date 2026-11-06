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
                exclude("**/build/**", "**/.gradle/**")
            },
        ).withPathSensitivity(PathSensitivity.RELATIVE)
        .withPropertyName("projectSources")
}
