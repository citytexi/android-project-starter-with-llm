plugins {
    id("starter.android.library")
    id("starter.android.compose")
    id("starter.android.screenshot")
}

android {
    namespace = "com.example.starter.core.designsystem"
}

dependencies {
    // Preview annotations (core.ui.preview). The automatic preview capture also needs
    // ScreenshotExcluded on the test classpath of every module that applies starter.android.screenshot.
    implementation(project(":core:ui"))
}
