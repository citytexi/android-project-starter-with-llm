package com.example.starter.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.example.starter.core.designsystem.theme.color.DarkColors
import com.example.starter.core.designsystem.theme.color.LightColors
import com.example.starter.core.designsystem.theme.color.MIN_UI_CONTRAST
import com.example.starter.core.designsystem.theme.color.StarterColors
import com.example.starter.core.designsystem.theme.color.assertContrast
import com.example.starter.core.designsystem.theme.dimension.DefaultRadius
import com.example.starter.core.designsystem.theme.typography.DefaultTypography
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Pins every Material 3 slot to its token, so a remapped slot fails here and not on a screen. */
class MaterialMappingTest {
    @Test
    fun lightColorSlots() = assertSlots(LightMaterialColorScheme, LightColors, LightMaterialOnlyColors)

    @Test
    fun darkColorSlots() = assertSlots(DarkMaterialColorScheme, DarkColors, DarkMaterialOnlyColors)

    @Test
    fun fixedSlotsAreTheSameInBothThemes() {
        val light = LightMaterialColorScheme
        val dark = DarkMaterialColorScheme
        assertEquals(light.fixedSlots(), dark.fixedSlots())
    }

    @Test
    fun lightSurfaceContainersGetDarker() {
        val levels = LightMaterialColorScheme.containerLevels().map { it.luminance() }
        assertTrue("light containers $levels", levels.zipWithNext().all { (a, b) -> a > b })
    }

    @Test
    fun darkSurfaceContainersGetLighter() {
        val levels = DarkMaterialColorScheme.containerLevels().map { it.luminance() }
        assertTrue("dark containers $levels", levels.zipWithNext().all { (a, b) -> a < b })
    }

    @Test
    fun navigationIndicatorIsVisibleOnItsBar() {
        listOf(LightMaterialColorScheme, DarkMaterialColorScheme).forEach {
            assertNotEquals(it.surfaceContainer, it.secondaryContainer)
        }
    }

    @Test
    fun primaryIsReadableAsTextAndAsBackground() {
        listOf(LightMaterialColorScheme, DarkMaterialColorScheme).forEach {
            assertContrast("primary on surface", it.primary, it.surface, MIN_UI_CONTRAST)
            assertContrast("onPrimary on primary", it.onPrimary, it.primary)
        }
    }

    @Test
    fun inversePrimaryIsReadableOnInverseSurface() {
        // Snackbar actions draw inversePrimary on inverseSurface.
        listOf(LightMaterialColorScheme, DarkMaterialColorScheme).forEach {
            assertContrast("inversePrimary on inverseSurface", it.inversePrimary, it.inverseSurface, MIN_UI_CONTRAST)
        }
    }

    @Test
    fun typographySlots() {
        val m = DefaultMaterialTypography
        val t = DefaultTypography
        assertEquals(t.t14Bold, m.displayLarge)
        assertEquals(t.t13Bold, m.displayMedium)
        assertEquals(t.t12Bold, m.displaySmall)
        assertEquals(t.t11Bold, m.headlineLarge)
        assertEquals(t.t10Bold, m.headlineMedium)
        assertEquals(t.t9Bold, m.headlineSmall)
        assertEquals(t.t7Bold, m.titleLarge)
        assertEquals(t.t5Bold, m.titleMedium)
        assertEquals(t.t4Bold, m.titleSmall)
        assertEquals(t.t5Regular, m.bodyLarge)
        assertEquals(t.t4Regular, m.bodyMedium)
        assertEquals(t.t3Regular, m.bodySmall)
        assertEquals(t.t4Medium, m.labelLarge)
        assertEquals(t.t3Medium, m.labelMedium)
        assertEquals(t.t2Medium, m.labelSmall)
    }

    @Test
    fun shapeSlots() {
        val s = DefaultMaterialShapes
        val r = DefaultRadius
        assertEquals(RoundedCornerShape(r.r1), s.extraSmall)
        assertEquals(RoundedCornerShape(r.r2), s.small)
        assertEquals(RoundedCornerShape(r.r3), s.medium)
        assertEquals(RoundedCornerShape(r.r4), s.large)
        assertEquals(RoundedCornerShape(r.r6), s.extraLarge)
    }

    private fun assertSlots(
        scheme: ColorScheme,
        colors: StarterColors,
        only: MaterialOnlyColors,
    ) {
        val bg = colors.bg
        val fg = colors.fg
        val stroke = colors.stroke
        val expected: List<Triple<String, Color, Color>> =
            listOf(
                Triple("primary", fg.brand, scheme.primary),
                Triple("onPrimary", only.onPrimary, scheme.onPrimary),
                Triple("primaryContainer", bg.brandWeak, scheme.primaryContainer),
                Triple("onPrimaryContainer", fg.brandContrast, scheme.onPrimaryContainer),
                Triple("inversePrimary", only.inversePrimary, scheme.inversePrimary),
                Triple("secondary", bg.neutralSolid, scheme.secondary),
                Triple("onSecondary", fg.onNeutralSolid, scheme.onSecondary),
                Triple("secondaryContainer", bg.neutralWeakPressed, scheme.secondaryContainer),
                Triple("onSecondaryContainer", fg.neutral, scheme.onSecondaryContainer),
                Triple("tertiary", bg.informativeSolid, scheme.tertiary),
                Triple("onTertiary", fg.onInformativeSolid, scheme.onTertiary),
                Triple("tertiaryContainer", bg.informativeWeak, scheme.tertiaryContainer),
                Triple("onTertiaryContainer", fg.informativeContrast, scheme.onTertiaryContainer),
                Triple("background", bg.layerDefault, scheme.background),
                Triple("onBackground", fg.neutral, scheme.onBackground),
                Triple("surface", bg.layerDefault, scheme.surface),
                Triple("onSurface", fg.neutral, scheme.onSurface),
                Triple("surfaceVariant", bg.neutralWeak, scheme.surfaceVariant),
                Triple("onSurfaceVariant", fg.neutralMuted, scheme.onSurfaceVariant),
                Triple("surfaceTint", Color.Transparent, scheme.surfaceTint),
                Triple("inverseSurface", bg.neutralInverted, scheme.inverseSurface),
                Triple("inverseOnSurface", fg.neutralInverted, scheme.inverseOnSurface),
                Triple("error", bg.criticalSolid, scheme.error),
                Triple("onError", fg.onCriticalSolid, scheme.onError),
                Triple("errorContainer", bg.criticalWeak, scheme.errorContainer),
                Triple("onErrorContainer", fg.criticalContrast, scheme.onErrorContainer),
                Triple("outline", stroke.neutralWeak, scheme.outline),
                Triple("outlineVariant", stroke.neutralMuted, scheme.outlineVariant),
                Triple("scrim", bg.overlay, scheme.scrim),
                Triple("surfaceBright", bg.layerFloating, scheme.surfaceBright),
                Triple("surfaceDim", bg.layerBasement, scheme.surfaceDim),
                Triple("surfaceContainerLowest", only.surfaceContainerLowest, scheme.surfaceContainerLowest),
                Triple("surfaceContainerLow", only.surfaceContainerLow, scheme.surfaceContainerLow),
                Triple("surfaceContainer", only.surfaceContainer, scheme.surfaceContainer),
                Triple("surfaceContainerHigh", only.surfaceContainerHigh, scheme.surfaceContainerHigh),
                Triple("surfaceContainerHighest", only.surfaceContainerHighest, scheme.surfaceContainerHighest),
                Triple("primaryFixed", LightColors.bg.brandWeak, scheme.primaryFixed),
                Triple("primaryFixedDim", LightColors.bg.brandWeakPressed, scheme.primaryFixedDim),
                Triple("onPrimaryFixed", LightColors.fg.brandContrast, scheme.onPrimaryFixed),
                Triple("onPrimaryFixedVariant", LightColors.fg.brand, scheme.onPrimaryFixedVariant),
                Triple("secondaryFixed", LightColors.bg.neutralWeak, scheme.secondaryFixed),
                Triple("secondaryFixedDim", LightColors.bg.neutralWeakPressed, scheme.secondaryFixedDim),
                Triple("onSecondaryFixed", LightColors.fg.neutral, scheme.onSecondaryFixed),
                Triple("onSecondaryFixedVariant", LightColors.fg.neutralMuted, scheme.onSecondaryFixedVariant),
                Triple("tertiaryFixed", LightColors.bg.informativeWeak, scheme.tertiaryFixed),
                Triple("tertiaryFixedDim", LightColors.bg.informativeWeakPressed, scheme.tertiaryFixedDim),
                Triple("onTertiaryFixed", LightColors.fg.informativeContrast, scheme.onTertiaryFixed),
                Triple("onTertiaryFixedVariant", LightColors.fg.informative, scheme.onTertiaryFixedVariant),
            )
        expected.forEach { (slot, token, actual) -> assertEquals(slot, token, actual) }
    }

    private fun ColorScheme.containerLevels() =
        listOf(surfaceContainerLowest, surfaceContainerLow, surfaceContainer, surfaceContainerHigh, surfaceContainerHighest)

    private fun ColorScheme.fixedSlots() =
        listOf(
            primaryFixed,
            primaryFixedDim,
            onPrimaryFixed,
            onPrimaryFixedVariant,
            secondaryFixed,
            secondaryFixedDim,
            onSecondaryFixed,
            onSecondaryFixedVariant,
            tertiaryFixed,
            tertiaryFixedDim,
            onTertiaryFixed,
            onTertiaryFixedVariant,
        )
}
