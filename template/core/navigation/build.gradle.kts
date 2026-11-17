// Placeholder. Every navigation variant ships its own copy of this file
// (variants/navigation/<nav>/core/navigation/build.gradle.kts) that adds the navigation
// libraries as `api`, and that copy replaces this one in the generated project.
plugins {
    id("starter.android.library")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.example.starter.core.navigation"
}
