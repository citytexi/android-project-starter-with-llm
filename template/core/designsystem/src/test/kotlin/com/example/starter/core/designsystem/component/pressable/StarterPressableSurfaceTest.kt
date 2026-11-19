package com.example.starter.core.designsystem.component.pressable

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PixelMap
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import com.example.starter.core.designsystem.R
import com.example.starter.core.designsystem.theme.StarterTheme
import com.example.starter.core.designsystem.theme.color.LightColors
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.math.abs
import kotlin.math.roundToInt

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w600dp-h600dp")
class StarterPressableSurfaceTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val source = MutableInteractionSource()
    private var lastPress: PressInteraction.Press? = null

    @Test
    fun contentScaleUsesSurfaceSize() {
        setWideContentSurface()
        press()
        val d = composeRule.density.density
        val row = centerRow()
        val content = row.count { it == CONTENT.toArgb() }
        val surface = row.count { it == BASE.toArgb() || it == PRESSED.toArgb() }
        val bySurface = (100 * d * pressedScaleFactor(300 * d, 40 * d, d)).roundToInt()
        val byContent = (100 * d * pressedScaleFactor(100 * d, 20 * d, d)).roundToInt()
        assertTrue("content $content should be near $bySurface", abs(content - bySurface) <= 1)
        assertNotEquals("ratio must not come from the content's own size", byContent, content)
        // Each end of the shrunk content lands on a fractional pixel that blends both colors, hence a tolerance of 2.
        assertTrue("surface does not shrink: ${surface + content}", abs(surface + content - (300 * d).roundToInt()) <= 2)
        assertEquals("left edge stays painted", PRESSED.toArgb(), row.first())
        assertEquals("right edge stays painted", PRESSED.toArgb(), row.last())
    }

    @Test
    fun contentScaleKeepsSurfaceColorSwap() {
        setWideContentSurface()
        press()
        val image = capture()
        val x = (5 * composeRule.density.density).roundToInt()
        assertEquals(PRESSED.toArgb(), image[x, image.height / 2].toArgb())
    }

    @Test
    fun singleSelectionSurfaceIsRadioButton() {
        var clicks = 0
        setSurface(selected = true, selectionMode = PressableSelectionMode.SINGLE, onClick = { clicks++ })
        val node = composeRule.onNodeWithTag(TAG)
        node
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.RadioButton))
            .assertIsSelected()
        assertFalse(node.fetchSemanticsNode().config.contains(SemanticsProperties.ToggleableState))
        node.performClick()
        assertEquals(1, clicks)
    }

    @Test
    fun toggleSelectionSurfaceStaysCheckbox() {
        setSurface(selected = false)
        composeRule
            .onNodeWithTag(TAG)
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Checkbox))
            .assertIsOff()
    }

    @Test
    fun pressedBorderShowsOnlyWhilePressed() {
        setSurface(scaleScope = PressScaleScope.CONTENT, pressedBorder = BorderStroke(4.dp, BORDER))
        assertEquals(BASE.toArgb(), borderPixel())
        press()
        assertEquals(BORDER.toArgb(), borderPixel())
        release()
        assertEquals(BASE.toArgb(), borderPixel())
    }

    @Test
    fun pressedBorderHiddenWhenDisabled() {
        setSurface(enabled = false, scaleScope = PressScaleScope.CONTENT, pressedBorder = BorderStroke(4.dp, BORDER))
        press()
        assertEquals(BASE.toArgb(), borderPixel())
    }

    @Test
    fun toggleSelectionSurfaceReportsOnAndClicks() {
        var clicks = 0
        setSurface(selected = true, onClick = { clicks++ })
        composeRule.onNodeWithTag(TAG).assertIsOn().performClick()
        assertEquals(1, clicks)
    }

    @Test
    fun loadingSurfaceAnnouncesLoading() {
        setSurface(loading = true)
        val loading = ApplicationProvider.getApplicationContext<Context>().getString(R.string.designsystem_loading)
        composeRule.onNodeWithTag(TAG).assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, loading))
    }

    @Test
    fun borderDrawsAtEdge() {
        setSurface(border = BorderStroke(4.dp, BORDER))
        assertEquals(BORDER.toArgb(), borderPixel())
    }

    @Test
    fun focusRingShowsOnlyWhileFocused() {
        setSurface()
        assertEquals(BASE.toArgb(), borderPixel())
        val focus = FocusInteraction.Focus()
        composeRule.runOnIdle { source.tryEmit(focus) }
        composeRule.waitForIdle()
        assertEquals(LightColors.stroke.focusRing.toArgb(), borderPixel())
        composeRule.runOnIdle { source.tryEmit(FocusInteraction.Unfocus(focus)) }
        composeRule.waitForIdle()
        assertEquals(BASE.toArgb(), borderPixel())
    }

    @Test
    fun clickLabelReachesSemantics() {
        setSurface(onClickLabel = "open")
        val action = composeRule.onNodeWithTag(TAG).fetchSemanticsNode().config[SemanticsActions.OnClick]
        assertEquals("open", action.label)
    }

    private fun setWideContentSurface() {
        composeRule.setContent {
            StarterTheme(darkTheme = false) {
                StarterPressableSurface(
                    color = BASE,
                    pressedColor = PRESSED,
                    shape = RectangleShape,
                    enabled = true,
                    onClick = {},
                    modifier = Modifier.testTag(TAG).size(300.dp, 40.dp),
                    scaleScope = PressScaleScope.CONTENT,
                    interactionSource = source,
                ) {
                    Box(Modifier.size(100.dp, 20.dp).background(CONTENT))
                }
            }
        }
        composeRule.waitForIdle()
    }

    private fun setSurface(
        enabled: Boolean = true,
        loading: Boolean = false,
        selected: Boolean? = null,
        selectionMode: PressableSelectionMode = PressableSelectionMode.TOGGLE,
        scaleScope: PressScaleScope = PressScaleScope.SELF,
        border: BorderStroke? = null,
        pressedBorder: BorderStroke? = null,
        onClickLabel: String? = null,
        size: Dp = 100.dp,
        onClick: () -> Unit = {},
    ) {
        composeRule.setContent {
            StarterTheme(darkTheme = false) {
                StarterPressableSurface(
                    color = BASE,
                    pressedColor = PRESSED,
                    shape = RectangleShape,
                    enabled = enabled,
                    onClick = onClick,
                    modifier = Modifier.testTag(TAG).size(size),
                    loading = loading,
                    selected = selected,
                    selectionMode = selectionMode,
                    scaleScope = scaleScope,
                    border = border,
                    pressedBorder = pressedBorder,
                    onClickLabel = onClickLabel,
                    interactionSource = source,
                ) {}
            }
        }
        composeRule.waitForIdle()
    }

    private fun capture(): PixelMap = composeRule.onNodeWithTag(TAG).captureToImage().toPixelMap()

    private fun centerRow(): List<Int> {
        val image = capture()
        val y = image.height / 2
        return (0 until image.width).map { image[it, y].toArgb() }
    }

    private fun borderPixel(): Int {
        val image = capture()
        return image[composeRule.density.density.roundToInt(), image.height / 2].toArgb()
    }

    /** A disabled surface emits no Press from taps, so the interaction is injected to test the surface's own guard. */
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

    private companion object {
        const val TAG = "surface"
        val BASE = Color(0xFF00FF00)
        val PRESSED = Color(0xFFFF0000)
        val CONTENT = Color(0xFFFFFF00)
        val BORDER = Color(0xFF0000FF)
    }
}
