plugins {
    id("starter.android.library")
}

android {
    namespace = "com.example.starter.core.testing.android"
}

dependencies {
    api(libs.androidx.test.runner)
    api(libs.hilt.android.testing)
}
