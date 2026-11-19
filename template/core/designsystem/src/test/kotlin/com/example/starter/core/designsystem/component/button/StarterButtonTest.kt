package com.example.starter.core.designsystem.component.button

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.example.starter.core.designsystem.theme.StarterTheme
import com.example.starter.core.designsystem.theme.color.LightColors
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode
import kotlin.math.roundToInt

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class StarterButtonTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val source = MutableInteractionSource()

    @Test
    fun clickCallsOnClick() {
        var clicks = 0
        setButton(enabled = true, onClick = { clicks++ })
        composeRule.onNodeWithTag(TAG).assertIsEnabled().performClick()
        assertEquals(1, clicks)
    }

    @Test
    fun disabledIgnoresClicks() {
        var clicks = 0
        setButton(enabled = false, onClick = { clicks++ })
        composeRule.onNodeWithTag(TAG).assertIsNotEnabled().performClick()
        assertEquals(0, clicks)
    }

    @Test
    fun pressDarkensBrandSurface() {
        setButton(enabled = true)
        assertEquals(LightColors.bg.brandSolid.toArgb(), surfacePixel())
        press()
        assertEquals(LightColors.bg.brandSolidPressed.toArgb(), surfacePixel())
    }

    @Test
    fun disabledUsesDisabledSurface() {
        setButton(enabled = false)
        assertEquals(LightColors.bg.disabled.toArgb(), surfacePixel())
        press()
        assertEquals(LightColors.bg.disabled.toArgb(), surfacePixel())
    }

    @Test
    fun meetsTouchTargetHeight() {
        setButton(enabled = true)
        val heightPx =
            composeRule
                .onNodeWithTag(TAG)
                .fetchSemanticsNode()
                .size.height
        assertTrue("height $heightPx", heightPx >= (48 * composeRule.density.density).roundToInt())
    }

    private fun setButton(
        enabled: Boolean,
        onClick: () -> Unit = {},
    ) {
        composeRule.setContent {
            StarterTheme(darkTheme = false) {
                StarterButton(
                    text = "Button",
                    onClick = onClick,
                    modifier = Modifier.testTag(TAG),
                    enabled = enabled,
                    interactionSource = source,
                )
            }
        }
        composeRule.waitForIdle()
    }

    /** A pixel 8dp in from the left edge at mid-height: inside the surface after the shrink, left of the label padding. */
    private fun surfacePixel(): Int {
        val image = composeRule.onNodeWithTag(TAG).captureToImage().toPixelMap()
        return image[(8 * composeRule.density.density).roundToInt(), image.height / 2].toArgb()
    }

    /** Disabled buttons emit no Press from taps, so the interaction is injected to test the surface's own guard. */
    private fun press() {
        composeRule.runOnIdle { source.tryEmit(PressInteraction.Press(Offset.Zero)) }
        composeRule.mainClock.advanceTimeBy(300)
        composeRule.waitForIdle()
    }

    private companion object {
        const val TAG = "button"
    }
}
