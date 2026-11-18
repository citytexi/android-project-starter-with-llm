package com.example.starter.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.Dp
import com.example.starter.core.designsystem.theme.dimension.DefaultDimension
import com.example.starter.core.designsystem.theme.dimension.DefaultRadius
import com.example.starter.core.designsystem.theme.motion.DefaultMotion
import com.example.starter.core.designsystem.theme.shadow.DarkShadows
import com.example.starter.core.designsystem.theme.shadow.LightShadows
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Guards the relations between tokens, not their values. Change the values per app; keep these:
 * the spacing scale is a grid of `x1`, every scale climbs strictly, and role values sit on the scale.
 */
class FoundationScaleTest {
    private val d = DefaultDimension

    /** Each spacing step and its multiple of `x1`. */
    private val spacingScale =
        listOf(
            d.x0Half to 0.5f,
            d.x1 to 1f,
            d.x1Half to 1.5f,
            d.x2 to 2f,
            d.x2Half to 2.5f,
            d.x3 to 3f,
            d.x3Half to 3.5f,
            d.x4 to 4f,
            d.x4Half to 4.5f,
            d.x5 to 5f,
            d.x6 to 6f,
            d.x7 to 7f,
            d.x8 to 8f,
            d.x9 to 9f,
            d.x10 to 10f,
            d.x12 to 12f,
            d.x14 to 14f,
            d.x16 to 16f,
        )

    @Test
    fun spacingIsAGridOfX1() {
        spacingScale.forEach { (step, multiple) -> assertEquals("x$multiple", d.x1 * multiple, step) }
    }

    @Test
    fun spacingScaleClimbs() = assertClimbs("spacing", spacingScale.map { it.first })

    @Test
    fun semanticSpacingUsesScaleSteps() {
        val scale = spacingScale.map { it.first }
        listOf(
            d.spacingX.globalGutter,
            d.spacingX.betweenChips,
            d.spacingY.betweenText,
            d.spacingY.componentDefault,
            d.spacingY.screenBottom,
        ).forEach { assertTrue("$it is not on the spacing scale", it in scale) }
    }

    @Test
    fun radiusScaleClimbs() {
        val r = DefaultRadius
        assertClimbs("radius", listOf(r.r0Half, r.r1, r.r1Half, r.r2, r.r2Half, r.r3, r.r3Half, r.r4, r.r5, r.r6))
        assertEquals(RoundedCornerShape(percent = 50), r.full)
    }

    @Test
    fun shadowsGrowWithElevation() {
        listOf(LightShadows, DarkShadows).forEach { s ->
            assertClimbs("shadow blur", listOf(s.s1.blur, s.s2.blur, s.s3.blur))
            assertClimbs("shadow offset", listOf(s.s1.offsetY, s.s2.offsetY, s.s3.offsetY))
        }
    }

    @Test
    fun darkShadowsAreStrongerThanLight() {
        listOf(
            LightShadows.s1 to DarkShadows.s1,
            LightShadows.s2 to DarkShadows.s2,
            LightShadows.s3 to DarkShadows.s3,
        ).forEach { (light, dark) -> assertTrue(dark.color.alpha > light.color.alpha) }
    }

    @Test
    fun durationScaleClimbs() {
        val duration = DefaultMotion.duration
        val scale = listOf(duration.d1, duration.d2, duration.d3, duration.d4, duration.d5, duration.d6)
        assertTrue("durations $scale", scale.zipWithNext().all { (a, b) -> a < b })
        assertTrue(duration.colorTransition in scale)
        assertTrue(duration.pressedScale in scale)
    }

    private fun assertClimbs(
        name: String,
        steps: List<Dp>,
    ) = assertTrue("$name $steps", steps.zipWithNext().all { (a, b) -> a < b })
}
