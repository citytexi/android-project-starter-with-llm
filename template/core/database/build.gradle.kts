plugins {
    id("starter.android.library")
    id("starter.hilt")
    alias(libs.plugins.room)
}

android {
    namespace = "com.example.starter.core.database"
    // Robolectric only loads unit-test assets with this on.
    testOptions.unitTests.isIncludeAndroidResources = true
}

// StarterDatabaseMigrationTest (Robolectric) reads the exported schemas as unit-test assets.
// The Room plugin only adds them to androidTest assets.
androidComponents {
    onVariants { variant ->
        variant.hostTests.values.forEach { it.sources.assets?.addStaticSourceDirectory("schemas") }
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    api(libs.androidx.room.runtime)
    api(libs.kotlinx.coroutines.core)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.room.testing)
}
