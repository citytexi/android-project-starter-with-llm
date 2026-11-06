plugins {
    id("starter.android.feature")
    id("starter.android.screenshot")
}

android {
    namespace = "com.example.starter.feature.sample"
}

// Navigation libraries come from :core:navigation as `api`. Its build file is shipped by
// the navigation variant (variants/navigation/<nav>/core/navigation/build.gradle.kts), so
// this file stays the same for every variant.
dependencies {
    testImplementation(project(":core:testing:jvm"))
}
