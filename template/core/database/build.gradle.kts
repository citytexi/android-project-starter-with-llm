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
