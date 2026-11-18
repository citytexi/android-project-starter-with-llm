package com.example.starter.core.designsystem.theme.color

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import org.junit.Assert.assertTrue

/** WCAG AA for body text. */
internal const val MIN_TEXT_CONTRAST = 4.5f

/** WCAG AA for UI parts and large text. */
internal const val MIN_UI_CONTRAST = 3f

/** WCAG contrast ratio, 1 to 21. */
internal fun contrastRatio(
    first: Color,
    second: Color,
): Float {
    val lighter = maxOf(first.luminance(), second.luminance())
    val darker = minOf(first.luminance(), second.luminance())
    return (lighter + 0.05f) / (darker + 0.05f)
}

internal fun assertContrast(
    name: String,
    foreground: Color,
    background: Color,
    minimum: Float = MIN_TEXT_CONTRAST,
) {
    val ratio = contrastRatio(foreground, background)
    assertTrue("$name contrast $ratio < $minimum", ratio >= minimum)
}
