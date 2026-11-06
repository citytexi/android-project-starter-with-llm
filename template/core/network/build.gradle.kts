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
