package com.example.starter.preview.settings

/** Theme chosen in the control bar. An enum so `rememberSaveable` keeps it without a custom saver. */
enum class PreviewThemeMode(
    val label: String,
) {
    LIGHT("라이트"),
    DARK("다크"),
    SYSTEM("시스템"),
}
