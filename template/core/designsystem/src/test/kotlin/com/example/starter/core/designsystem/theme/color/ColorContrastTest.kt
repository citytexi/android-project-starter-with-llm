package com.example.starter.core.designsystem.theme.color

import org.junit.Test

/** Text on solid backgrounds, idle and pressed, must meet WCAG AA (4.5:1). Run it again after changing the palette. */
class ColorContrastTest {
    @Test
    fun lightOnSolidTextIsReadable() = assertReadable(LightColors)

    @Test
    fun darkOnSolidTextIsReadable() = assertReadable(DarkColors)

    @Test
    fun neutralTextOnLayerIsReadable() {
        listOf(LightColors, DarkColors).forEach { colors ->
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
        assertContrast("onBrandSolid pressed", fg.onBrandSolid, bg.brandSolidPressed)
        assertContrast("onCriticalSolid pressed", fg.onCriticalSolid, bg.criticalSolidPressed)
        assertContrast("onInformativeSolid pressed", fg.onInformativeSolid, bg.informativeSolidPressed)
        assertContrast("onNeutralSolid pressed", fg.onNeutralSolid, bg.neutralSolidPressed)
        assertContrast("onPositiveSolid pressed", fg.onPositiveSolid, bg.positiveSolidPressed)
        assertContrast("onWarningSolid pressed", fg.onWarningSolid, bg.warningSolidPressed)
    }
}
