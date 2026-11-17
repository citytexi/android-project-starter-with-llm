plugins {
    id("starter.android.library")
    id("starter.hilt")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.example.starter.core.network"
    buildFeatures {
        buildConfig = true
    }
    defaultConfig {
        // `starter.baseUrl` in gradle.properties, written by scripts/generate.py from the PRD.
        buildConfigField("String", "BASE_URL", "\"${providers.gradleProperty("starter.baseUrl").get()}\"")
    }
}

dependencies {
    api(libs.retrofit)
    api(libs.kotlinx.serialization.json)
    implementation(libs.retrofit.kotlinx.serialization.converter)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)

    testImplementation(libs.okhttp.mockwebserver)
}
