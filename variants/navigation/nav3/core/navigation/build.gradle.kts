// nav3 variant: replaces template/core/navigation/build.gradle.kts at generation time.
// The navigation libraries are `api` so :feature:* and :app get them through
// :core:navigation, and their own build files stay the same for every variant.
plugins {
    id("starter.android.library")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.example.starter.core.navigation"
}

dependencies {
    api(libs.androidx.navigation3.runtime)
    api(libs.androidx.navigation3.ui)
    api(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.kotlinx.serialization.json)
}
