package com.example.starter.preview.settings

private const val NORMAL_FONT_SCALE = 1f
private const val LARGE_FONT_SCALE = 1.3f
private const val HUGE_FONT_SCALE = 2f

/**
 * Font scale chosen in the control bar. [scale] is absolute and replaces the system font size
 * setting instead of multiplying it.
 */
enum class PreviewFontScale(
    val scale: Float,
    val label: String,
) {
    NORMAL(NORMAL_FONT_SCALE, "1×"),
    LARGE(LARGE_FONT_SCALE, "1.3×"),
    HUGE(HUGE_FONT_SCALE, "2×"),
}
