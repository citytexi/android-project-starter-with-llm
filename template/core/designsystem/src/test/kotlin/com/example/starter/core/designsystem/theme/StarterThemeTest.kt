package com.example.starter.core.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.starter.core.designsystem.theme.color.DarkColors
import com.example.starter.core.designsystem.theme.color.LightColors
import com.example.starter.core.designsystem.theme.color.StarterColors
import com.example.starter.core.designsystem.theme.dimension.DefaultDimension
import com.example.starter.core.designsystem.theme.dimension.DefaultRadius
import com.example.starter.core.designsystem.theme.dimension.StarterDimension
import com.example.starter.core.designsystem.theme.dimension.StarterRadius
import com.example.starter.core.designsystem.theme.motion.DefaultMotion
import com.example.starter.core.designsystem.theme.motion.StarterMotion
import com.example.starter.core.designsystem.theme.shadow.DarkShadows
import com.example.starter.core.designsystem.theme.shadow.LightShadows
import com.example.starter.core.designsystem.theme.shadow.StarterShadows
import com.example.starter.core.designsystem.theme.typography.DefaultTypography
import com.example.starter.core.designsystem.theme.typography.StarterTypography
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/** Checks what StarterTheme provides. The slot-by-slot Material mapping is in MaterialMappingTest. */
@RunWith(RobolectricTestRunner::class)
class StarterThemeTest {
    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var scheme: ColorScheme
    private lateinit var materialTypography: Typography
    private lateinit var shapes: Shapes
    private lateinit var colors: StarterColors
    private lateinit var typography: StarterTypography
    private lateinit var dimension: StarterDimension
    private lateinit var radius: StarterRadius
    private lateinit var shadows: StarterShadows
    private lateinit var motion: StarterMotion

    private fun captureThemed(darkTheme: Boolean) {
        composeRule.setContent {
            StarterTheme(darkTheme = darkTheme) {
                scheme = MaterialTheme.colorScheme
                materialTypography = MaterialTheme.typography
                shapes = MaterialTheme.shapes
                colors = StarterTheme.colors
                typography = StarterTheme.typography
                dimension = StarterTheme.dimension
                radius = StarterTheme.radius
                shadows = StarterTheme.shadows
                motion = StarterTheme.motion
            }
        }
        composeRule.waitForIdle()
    }

    @Test
    fun lightProvidesLightTokens() {
        captureThemed(darkTheme = false)
        assertSame(LightColors, colors)
        assertSame(LightShadows, shadows)
        assertSame(LightMaterialColorScheme, scheme)
        assertSharedTokens()
    }

    @Test
    fun darkProvidesDarkTokens() {
        captureThemed(darkTheme = true)
        assertSame(DarkColors, colors)
        assertSame(DarkShadows, shadows)
        assertSame(DarkMaterialColorScheme, scheme)
        assertSharedTokens()
    }

    @Test
    fun defaultsWithoutThemeAreLight() {
        composeRule.setContent {
            colors = StarterTheme.colors
            shadows = StarterTheme.shadows
        }
        composeRule.waitForIdle()
        assertEquals(LightColors, colors)
        assertEquals(LightShadows, shadows)
    }

    /** Tokens that do not depend on light or dark. */
    private fun assertSharedTokens() {
        assertSame(DefaultTypography, typography)
        assertSame(DefaultDimension, dimension)
        assertSame(DefaultRadius, radius)
        assertSame(DefaultMotion, motion)
        assertSame(DefaultMaterialTypography, materialTypography)
        assertSame(DefaultMaterialShapes, shapes)
    }
}
