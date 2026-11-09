plugins {
    id("starter.android.application")
    id("starter.android.compose")
    id("starter.hilt")
}

android {
    namespace = "com.example.starter"
    defaultConfig {
        applicationId = "com.example.starter"
        // Boots instrumented tests with HiltTestApplication.
        testInstrumentationRunner = "com.example.starter.core.testing.android.StarterTestRunner"
    }
}

// Navigation libraries come from :core:navigation as `api`. Its build file is shipped by
// the navigation variant, so this file stays the same for every variant.
dependencies {
    implementation(project(":feature:sample"))
    implementation(project(":data"))
    implementation(project(":core:common"))
    implementation(project(":core:datastore"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:navigation"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    testImplementation(project(":core:testing:jvm"))
    androidTestImplementation(project(":core:testing:android"))
}
