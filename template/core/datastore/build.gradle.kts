plugins {
    id("starter.android.library")
    id("starter.hilt")
}

android {
    namespace = "com.example.starter.core.datastore"
}

dependencies {
    api(libs.androidx.datastore.preferences)
    api(libs.kotlinx.coroutines.core)

    testImplementation(project(":core:testing:android"))
}
