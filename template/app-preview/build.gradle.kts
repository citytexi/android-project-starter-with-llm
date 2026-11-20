plugins {
    id("starter.android.application")
    id("starter.android.compose")
    id("starter.android.screenshot")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.example.starter.preview"
    defaultConfig {
        applicationId = "com.example.starter.preview"
    }
}

// Debug-only tool app: it is never shipped, so the release variant is switched off instead of
// removing the build type, which would clash with the shared application convention.
androidComponents {
    beforeVariants(selector().withBuildType("release")) { it.enable = false }
}

// Navigation libraries come from :core:navigation as `api`, like :app. Only the
// navigation/ package differs per navigation variant.
dependencies {
    implementation(project(":core:designsystem"))
    implementation(project(":core:ui"))
    implementation(project(":core:navigation"))

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
}
