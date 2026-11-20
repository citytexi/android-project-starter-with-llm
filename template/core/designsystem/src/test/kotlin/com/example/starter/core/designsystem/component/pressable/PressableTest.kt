package com.example.starter.core.designsystem.component.pressable

import android.content.Context
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReusableContent
import androidx.compose.runtime.ReusableContentHost
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import com.example.starter.core.designsystem.theme.StarterTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class PressableTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val source = MutableInteractionSource()
    private var lastPress: PressInteraction.Press? = null

    @Test
    fun scaleUsesHeightWhenTallest() =
        assertEquals((40f - 2f) / 40f, pressedScaleFactor(widthPx = 100f, heightPx = 40f, density = 1f), 1e-4f)

    @Test
    fun scaleUsesQuarterWidthForWideElements() = assertEquals((80f - 2f) / 80f, pressedScaleFactor(320f, 40f, 1f), 1e-4f)

    @Test
    fun scaleUses24dpFloorForSmallElements() = assertEquals((48f - 4f) / 48f, pressedScaleFactor(40f, 20f, 2f), 1e-4f)

    @Test
    fun enabledShrinksWhenPressed() {
        setBox(enabled = true)
        val before = paintedWidth()
        press()
        assertTrue("pressed width ${paintedWidth()} should be below $before", paintedWidth() < before)
    }

    @Test
    fun disabledDoesNotShrink() {
        setBox(enabled = false)
        val before = paintedWidth()
        press()
        assertEquals(before, paintedWidth())
    }

    @Test
    fun reduceMotionToggledWhileShownTakesEffect() {
        setBox(enabled = true)
        val idle = paintedWidth()
        setAnimatorScale(0f)
        press()
        assertEquals("no shrink while animations are removed", idle, paintedWidth())
        release()
        setAnimatorScale(1f)
        press()
        assertTrue("shrinks again once animations are back", paintedWidth() < idle)
    }

    @Test
    fun manyPressablesShareOneObserver() {
        composeRule.setContent {
            StarterTheme(darkTheme = false) {
                Column {
                    repeat(20) {
                        Box(
                            Modifier.size(20.dp).pressable(
                                interactionSource = remember { MutableInteractionSource() },
                                color = BASE,
                                pressedColor = PRESSED,
                                shape = RoundedCornerShape(0.dp),
                                enabled = true,
                                loading = false,
                                onClick = {},
                            ),
                        )
                    }
                }
            }
        }
        composeRule.waitForIdle()
        val resolver = ApplicationProvider.getApplicationContext<Context>().contentResolver
        val uri = Settings.Global.getUriFor(Settings.Global.ANIMATOR_DURATION_SCALE)
        assertEquals(1, shadowOf(resolver).getContentObservers(uri).size)
    }

    @Test
    fun loadingIsNotDisabled() {
        var clicks = 0
        setBox(enabled = true, loading = true, onClick = { clicks++ })
        composeRule.onNode(hasClickAction()).assertIsEnabled().performClick()
        assertEquals(0, clicks)
        assertFalse(
            composeRule
                .onNode(hasClickAction())
                .fetchSemanticsNode()
                .config
                .contains(SemanticsProperties.Disabled),
        )
    }

    @Test
    fun edgeTapSurvivesShrink() {
        var clicks = 0
        setBox(enabled = true, onClick = { clicks++ })
        val node = composeRule.onNode(hasClickAction())
        // After the shrink, 0.5px from the edge is outside the shrunk surface.
        // If the tap target shrank too, a slight move would cancel the tap.
        node.performTouchInput { down(Offset(0.5f, centerY)) }
        composeRule.mainClock.advanceTimeBy(300)
        node.performTouchInput {
            moveBy(Offset(0.2f, 0f))
            up()
        }
        composeRule.waitForIdle()
        assertEquals(1, clicks)
    }

    @Test
    fun loadingDoesNotShrinkOnPress() {
        setBox(enabled = true, loading = true)
        val before = paintedWidth()
        press()
        assertEquals(before, paintedWidth())
    }

    @Test
    fun loadingHoldsPressedColorWithoutPress() {
        setBox(enabled = true, loading = true)
        val image = composeRule.onRoot().captureToImage().toPixelMap()
        assertEquals(PRESSED.toArgb(), image[image.width / 2, image.height / 2].toArgb())
    }

    @Test
    fun pressedSwapsBackgroundOnly() {
        setBox(enabled = true) {
            Box(Modifier.align(Alignment.Center).size(20.dp).background(CONTENT))
        }
        press()
        val image = composeRule.onRoot().captureToImage().toPixelMap()
        val cx = image.width / 2
        val cy = image.height / 2
        // The root is 140dp, so 30dp below the center is inside the pressed surface and outside the 20dp content.
        val offset = (image.height * 30 / 140)
        assertEquals("background swaps", PRESSED.toArgb(), image[cx, cy + offset].toArgb())
        assertEquals("content keeps its color", CONTENT.toArgb(), image[cx, cy].toArgb())
    }

    @Test
    fun colorChangeReappliesWithoutPress() {
        var color by mutableStateOf(BASE)
        composeRule.setContent {
            Box(
                Modifier.size(100.dp).pressable(
                    interactionSource = remember { source },
                    color = color,
                    pressedColor = PRESSED,
                    shape = RoundedCornerShape(0.dp),
                    enabled = true,
                    loading = false,
                    onClick = {},
                ),
            )
        }
        color = OTHER
        composeRule.mainClock.advanceTimeBy(300)
        val image = composeRule.onRoot().captureToImage().toPixelMap()
        assertEquals(OTHER.toArgb(), image[image.width / 2, image.height / 2].toArgb())
    }

    @Test
    fun disabledIgnoresClicks() {
        var clicks = 0
        setBox(enabled = false, onClick = { clicks++ })
        composeRule.onNode(hasClickAction()).assertIsNotEnabled().performClick()
        assertEquals(0, clicks)
    }

    @Test
    fun disabledPressKeepsColor() {
        setBox(enabled = false)
        press()
        assertEquals(BASE.toArgb(), centerPixel())
    }

    @Test
    fun cancelRestoresSurface() {
        setBox(enabled = true)
        val idle = paintedWidth()
        press()
        composeRule.runOnIdle { lastPress?.let { source.tryEmit(PressInteraction.Cancel(it)) } }
        composeRule.mainClock.advanceTimeBy(300)
        assertEquals("size comes back after a cancelled press", idle, paintedWidth())
        assertEquals("color comes back after a cancelled press", BASE.toArgb(), centerPixel())
    }

    @Test
    fun newInteractionSourceIsTracked() {
        val other = MutableInteractionSource()
        var current by mutableStateOf(source)
        composeRule.setContent {
            StarterTheme(darkTheme = false) {
                Box(Modifier.size(140.dp), contentAlignment = Alignment.Center) {
                    Box(Modifier.size(100.dp).pressable(current, BASE, PRESSED, RoundedCornerShape(0.dp), true, false, {}))
                }
            }
        }
        composeRule.waitForIdle()
        val idle = paintedWidth()
        current = other
        composeRule.waitForIdle()
        composeRule.runOnIdle { other.tryEmit(PressInteraction.Press(Offset.Zero)) }
        composeRule.mainClock.advanceTimeBy(300)
        assertTrue("shrinks on the new source", paintedWidth() < idle)
        assertEquals("darkens on the new source", PRESSED.toArgb(), centerPixel())
    }

    @Test
    fun reattachedNodeStartsUnpressed() {
        var active by mutableStateOf(true)
        composeRule.setContent {
            StarterTheme(darkTheme = false) {
                Box(Modifier.size(140.dp), contentAlignment = Alignment.Center) {
                    ReusableContentHost(active) {
                        Box(Modifier.size(100.dp).pressable(source, BASE, PRESSED, RoundedCornerShape(0.dp), true, false, {}))
                    }
                }
            }
        }
        composeRule.waitForIdle()
        val idle = paintedWidth()
        press()
        // Deactivated while pressed: the release never reaches the node.
        active = false
        composeRule.waitForIdle()
        active = true
        composeRule.mainClock.advanceTimeBy(300)
        assertEquals(idle, paintedWidth())
        assertEquals(BASE.toArgb(), centerPixel())
    }

    @Test
    fun reusedNodeJumpsToNewColor() {
        var key by mutableStateOf(0)
        var color by mutableStateOf(BASE)
        composeRule.setContent {
            ReusableContent(key) {
                Box(Modifier.size(100.dp).pressable(source, color, PRESSED, RoundedCornerShape(0.dp), true, false, {}))
            }
        }
        composeRule.waitForIdle()
        composeRule.mainClock.autoAdvance = false
        composeRule.runOnIdle {
            key = 1
            color = OTHER
        }
        // Frame by frame: the reuse lands within a couple of frames, while a fade (colorTransition, 150ms) needs about ten.
        val frames =
            List(3) {
                composeRule.mainClock.advanceTimeByFrame()
                centerPixel()
            }
        assertEquals("a reused node must not fade from the previous item's color", OTHER.toArgb(), frames.last())
    }

    private fun setBox(
        enabled: Boolean,
        loading: Boolean = false,
        size: Dp = 100.dp,
        onClick: () -> Unit = {},
        content: @Composable BoxScope.() -> Unit = {},
    ) {
        composeRule.setContent {
            StarterTheme(darkTheme = false) {
                Box(Modifier.size(size * 1.4f), contentAlignment = Alignment.Center) {
                    Box(
                        Modifier
                            .size(size)
                            .pressable(
                                interactionSource = remember { source },
                                color = BASE,
                                pressedColor = PRESSED,
                                shape = RoundedCornerShape(0.dp),
                                enabled = enabled,
                                loading = loading,
                                onClick = onClick,
                            ),
                        content = content,
                    )
                }
            }
        }
        composeRule.waitForIdle()
    }

    private fun centerPixel(): Int {
        val image = composeRule.onRoot().captureToImage().toPixelMap()
        return image[image.width / 2, image.height / 2].toArgb()
    }

    /** Surface-colored pixels on the center row. Measured in pixels because the layout size stays and only the graphics layer shrinks. */
    private fun paintedWidth(): Int {
        val image = composeRule.onRoot().captureToImage().toPixelMap()
        val y = image.height / 2
        return (0 until image.width).count { image[it, y].toArgb() in SURFACE_COLORS }
    }

    /** clickable emits no Press while disabled or loading, so the interaction is injected to test the modifier's own guard. */
    private fun press() {
        composeRule.runOnIdle { lastPress = PressInteraction.Press(Offset.Zero).also { source.tryEmit(it) } }
        composeRule.mainClock.advanceTimeBy(300)
        composeRule.waitForIdle()
    }

    private fun release() {
        composeRule.runOnIdle { lastPress?.let { source.tryEmit(PressInteraction.Release(it)) } }
        composeRule.mainClock.advanceTimeBy(300)
        composeRule.waitForIdle()
    }

    private fun setAnimatorScale(scale: Float) {
        val resolver = ApplicationProvider.getApplicationContext<Context>().contentResolver
        Settings.Global.putFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, scale)
        composeRule.waitForIdle()
    }

    private companion object {
        val BASE = Color(0xFF00FF00)
        val PRESSED = Color(0xFFFF0000)
        val OTHER = Color(0xFF0000FF)
        val CONTENT = Color(0xFFFFFF00)
        val SURFACE_COLORS = setOf(BASE.toArgb(), PRESSED.toArgb())
    }
}
