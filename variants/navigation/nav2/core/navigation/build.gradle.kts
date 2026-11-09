// nav2 variant: replaces template/core/navigation/build.gradle.kts at generation time.
// The navigation library is `api` so :feature:* and :app get it through
// :core:navigation, and their own build files stay the same for every variant.
plugins {
    id("starter.android.library")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.example.starter.core.navigation"
}

dependencies {
    api(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)
}
