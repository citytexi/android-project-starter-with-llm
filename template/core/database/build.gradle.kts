plugins {
    id("starter.android.library")
    id("starter.hilt")
    alias(libs.plugins.room)
}

android {
    namespace = "com.example.starter.core.database"
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
}
