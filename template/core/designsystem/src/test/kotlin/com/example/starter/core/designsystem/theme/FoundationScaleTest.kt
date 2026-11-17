package com.example.starter.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import com.example.starter.core.designsystem.theme.dimension.DefaultStarterDimension
import com.example.starter.core.designsystem.theme.dimension.DefaultStarterRadius
import com.example.starter.core.designsystem.theme.motion.DefaultStarterMotion
import com.example.starter.core.designsystem.theme.shadow.DarkStarterShadows
import com.example.starter.core.designsystem.theme.shadow.LightStarterShadows
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Guards the relations between tokens. Change the values freely, but keep these relations. */
class FoundationScaleTest {
    @Test
    fun dimensionScaleIsFourDpGrid() {
        val d = DefaultStarterDimension
        assertEquals(2.dp, d.x0Half)
        assertEquals(4.dp, d.x1)
        assertEquals(16.dp, d.x4)
        assertEquals(64.dp, d.x16)
    }

    @Test
    fun semanticSpacingUsesScaleSteps() {
        val d = DefaultStarterDimension
        val scale =
            listOf(
                d.x0Half,
                d.x1,
                d.x1Half,
                d.x2,
                d.x2Half,
                d.x3,
                d.x3Half,
                d.x4,
                d.x4Half,
                d.x5,
                d.x6,
                d.x7,
                d.x8,
                d.x9,
                d.x10,
                d.x12,
                d.x14,
                d.x16,
            )
        val semantic =
            listOf(
                d.spacingX.globalGutter,
                d.spacingX.betweenChips,
                d.spacingY.betweenText,
                d.spacingY.componentDefault,
                d.spacingY.screenBottom,
            )
        semantic.forEach { assertTrue("$it is not on the spacing scale", it in scale) }
    }

    @Test
    fun radius() {
        assertEquals(2.dp, DefaultStarterRadius.r0Half)
        assertEquals(24.dp, DefaultStarterRadius.r6)
        assertEquals(RoundedCornerShape(percent = 50), DefaultStarterRadius.full)
    }

    @Test
    fun shadowsGrowWithElevation() {
        listOf(LightStarterShadows, DarkStarterShadows).forEach { shadows ->
            assertTrue(shadows.s1.blur < shadows.s2.blur && shadows.s2.blur < shadows.s3.blur)
            assertTrue(shadows.s1.offsetY < shadows.s2.offsetY && shadows.s2.offsetY < shadows.s3.offsetY)
        }
    }

    @Test
    fun darkShadowsAreStrongerThanLight() {
        assertTrue(DarkStarterShadows.s1.color.alpha > LightStarterShadows.s1.color.alpha)
        assertTrue(DarkStarterShadows.s3.color.alpha > LightStarterShadows.s3.color.alpha)
    }

    @Test
    fun durationScaleAscends() {
        val duration = DefaultStarterMotion.duration
        val scale = listOf(duration.d1, duration.d2, duration.d3, duration.d4, duration.d5, duration.d6)
        assertEquals(scale.sorted(), scale)
        assertTrue(duration.colorTransition in scale)
        assertTrue(duration.pressedScale in scale)
    }
}
