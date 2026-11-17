package com.example.starter.core.designsystem.theme.color

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import org.junit.Assert.assertTrue
import org.junit.Test

/** Text on solid backgrounds must meet WCAG AA (4.5:1). Run it again after changing the palette. */
class ColorContrastTest {
    @Test
    fun lightOnSolidTextIsReadable() = assertReadable(LightStarterColors)

    @Test
    fun darkOnSolidTextIsReadable() = assertReadable(DarkStarterColors)

    @Test
    fun neutralTextOnLayerIsReadable() {
        listOf(LightStarterColors, DarkStarterColors).forEach { colors ->
            assertContrast("fg.neutral on bg.layerDefault", colors.fg.neutral, colors.bg.layerDefault)
        }
    }

    private fun assertReadable(colors: StarterColors) {
        val bg = colors.bg
        val fg = colors.fg
        assertContrast("onBrandSolid", fg.onBrandSolid, bg.brandSolid)
        assertContrast("onCriticalSolid", fg.onCriticalSolid, bg.criticalSolid)
        assertContrast("onInformativeSolid", fg.onInformativeSolid, bg.informativeSolid)
        assertContrast("onNeutralSolid", fg.onNeutralSolid, bg.neutralSolid)
        assertContrast("onPositiveSolid", fg.onPositiveSolid, bg.positiveSolid)
        assertContrast("onWarningSolid", fg.onWarningSolid, bg.warningSolid)
    }

    private fun assertContrast(
        name: String,
        foreground: Color,
        background: Color,
    ) {
        val lighter = maxOf(foreground.luminance(), background.luminance())
        val darker = minOf(foreground.luminance(), background.luminance())
        val ratio = (lighter + 0.05f) / (darker + 0.05f)
        assertTrue("$name contrast $ratio < $MIN_CONTRAST", ratio >= MIN_CONTRAST)
    }

    private companion object {
        const val MIN_CONTRAST = 4.5f
    }
}
