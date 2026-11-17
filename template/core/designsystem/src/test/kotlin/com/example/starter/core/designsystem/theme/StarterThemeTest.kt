package com.example.starter.core.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.text.TextStyle
import com.example.starter.core.designsystem.theme.color.DarkStarterColors
import com.example.starter.core.designsystem.theme.color.LightStarterColors
import com.example.starter.core.designsystem.theme.color.StarterColors
import com.example.starter.core.designsystem.theme.typography.DefaultStarterTypography
import com.example.starter.core.designsystem.theme.typography.StarterTypography
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class StarterThemeTest {
    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var scheme: ColorScheme
    private lateinit var typography: Typography
    private lateinit var colors: StarterColors

    private fun captureThemed(darkTheme: Boolean) {
        composeRule.setContent {
            StarterTheme(darkTheme = darkTheme) {
                scheme = MaterialTheme.colorScheme
                typography = MaterialTheme.typography
                colors = StarterTheme.color
            }
        }
        composeRule.waitForIdle()
    }

    @Test
    fun lightMapsBrandIntoMaterial() {
        captureThemed(darkTheme = false)
        assertEquals(LightStarterColors, colors)
        assertEquals(colors.bg.brandSolid, scheme.primary)
        assertEquals(colors.fg.onBrandSolid, scheme.onPrimary)
        assertEquals(colors.fg.brandContrast, scheme.onPrimaryContainer)
        assertEquals(Color.Transparent, scheme.surfaceTint)
    }

    @Test
    fun darkSelectsDarkTokens() {
        captureThemed(darkTheme = true)
        assertEquals(DarkStarterColors, colors)
        assertEquals(DarkStarterColors.bg.brandSolid, scheme.primary)
        assertEquals(DarkStarterColors.bg.layerDefault, scheme.surface)
    }

    @Test
    fun outlineUsesNeutralWeak() {
        captureThemed(darkTheme = false)
        assertEquals(colors.stroke.neutralWeak, scheme.outline)
    }

    @Test
    fun everyMaterialTextSlotComesFromTokens() {
        captureThemed(darkTheme = false)
        val tokens = DefaultStarterTypography.allStyles()
        val t = typography
        listOf(
            t.displayLarge,
            t.displayMedium,
            t.displaySmall,
            t.headlineLarge,
            t.headlineMedium,
            t.headlineSmall,
            t.titleLarge,
            t.titleMedium,
            t.titleSmall,
            t.bodyLarge,
            t.bodyMedium,
            t.bodySmall,
            t.labelLarge,
            t.labelMedium,
            t.labelSmall,
        ).forEach { assertTrue("Material slot $it is not a token style", it in tokens) }
    }

    @Test
    fun defaultsWithoutThemeAreLight() {
        composeRule.setContent { colors = StarterTheme.color }
        composeRule.waitForIdle()
        assertEquals(LightStarterColors, colors)
    }

    private fun StarterTypography.allStyles(): Set<TextStyle> =
        StarterTypography::class.java.declaredFields
            .filter { it.type == TextStyle::class.java }
            .map { field -> field.apply { isAccessible = true }.get(this) as TextStyle }
            .toSet()
}
