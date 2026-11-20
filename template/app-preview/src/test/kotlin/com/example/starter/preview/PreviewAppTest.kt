package com.example.starter.preview

import androidx.activity.ComponentActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import com.example.starter.core.designsystem.theme.StarterTheme
import com.example.starter.preview.catalog.PreviewCatalogItem
import com.example.starter.preview.catalog.PreviewCategory
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Runs against whichever navigation variant generated the project; both provide the same PreviewNavHost. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class PreviewAppTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private var lightLayer = Color.Unspecified
    private var darkLayer = Color.Unspecified
    private var detailFontScale = 0f
    private var detailBackground = Color.Unspecified

    // Stands in for a real catalog screen: records what it received and has a back button.
    private val detail =
        PreviewCatalogItem(PreviewCategory.FOUNDATION, DETAIL) { onBack ->
            val fontScale = LocalDensity.current.fontScale
            val background = StarterTheme.colors.bg.layerDefault
            SideEffect {
                detailFontScale = fontScale
                detailBackground = background
            }
            Column {
                Text(DETAIL_BODY)
                Text(BACK, Modifier.clickable(onClick = onBack))
            }
        }

    @Test
    fun `choosing dark in the control bar paints the home with the dark layerDefault`() {
        setApp()
        composeRule.onNodeWithText("라이트").performClick()
        assertEquals(lightLayer, homeBackground())

        composeRule.onNodeWithText("다크").performClick()
        assertEquals(darkLayer, homeBackground())
    }

    @Test
    fun `an empty catalog says so on the home`() {
        composeRule.setContent { PreviewApp(catalog = emptyList()) }
        composeRule.onNodeWithText("등록된 화면이 없습니다").assertExists()
    }

    @Test
    fun `tapping a row opens its screen and back returns home`() {
        setApp()
        composeRule.onNodeWithText(DETAIL).performClick()
        composeRule.onNodeWithText(DETAIL_BODY).assertExists()

        composeRule.onNodeWithText(BACK).performClick()
        composeRule.onNodeWithText(DETAIL_BODY).assertDoesNotExist()
        composeRule.onNodeWithText(DETAIL).assertExists()
    }

    @Test
    fun `theme and font scale from the control bar reach the open screen`() {
        setApp()
        composeRule.onNodeWithText("라이트").performClick()
        composeRule.onNodeWithText(DETAIL).performClick()
        composeRule.waitForIdle()
        assertEquals(lightLayer, detailBackground)
        assertEquals(1f, detailFontScale)

        composeRule.onNodeWithText("다크").performClick()
        composeRule.onNodeWithText("2×").performClick()
        composeRule.waitForIdle()
        assertEquals(darkLayer, detailBackground)
        assertEquals(2f, detailFontScale)
    }

    @Test
    fun `tapping back twice quickly on a screen returns home without finishing the app`() {
        setApp()
        composeRule.onNodeWithText(DETAIL).performClick()
        // Both taps hit the same back button before any recomposition, like two taps inside one pop transition.
        val tapBack =
            composeRule
                .onNodeWithText(BACK)
                .fetchSemanticsNode()
                .config[SemanticsActions.OnClick]
                .action
        composeRule.runOnUiThread {
            tapBack?.invoke()
            tapBack?.invoke()
        }
        composeRule.waitForIdle()
        assertFalse(composeRule.activity.isFinishing)
        composeRule.onNodeWithText(DETAIL).assertExists()
    }

    @Test
    fun `system back on a screen returns home, and on the home closes the app`() {
        setApp()
        composeRule.onNodeWithText(DETAIL).performClick()
        composeRule.waitForIdle()
        pressBack()
        composeRule.onNodeWithText(DETAIL_BODY).assertDoesNotExist()
        assertFalse(composeRule.activity.isFinishing)

        // The test activity is no launcher root, so its default back would finish too: check that the
        // app's own handler takes back on the home (a real launcher root would only move to the background).
        assertTrue(composeRule.activity.onBackPressedDispatcher.hasEnabledCallbacks(), "no back handler on the home")
        pressBack()
        assertTrue(composeRule.activity.isFinishing)
    }

    private fun pressBack() {
        composeRule.runOnUiThread { composeRule.activity.onBackPressedDispatcher.onBackPressed() }
        composeRule.waitForIdle()
    }

    /** Sets the app with [detail] and records light and dark `bg.layerDefault` from the theme itself. */
    private fun setApp() {
        composeRule.setContent {
            StarterTheme(darkTheme = false) {
                val color = StarterTheme.colors.bg.layerDefault
                SideEffect { lightLayer = color }
            }
            StarterTheme(darkTheme = true) {
                val color = StarterTheme.colors.bg.layerDefault
                SideEffect { darkLayer = color }
            }
            PreviewApp(catalog = listOf(detail))
        }
    }

    // The home draws no surface of its own above PreviewApp's root, so the top-left pixel is its background.
    private fun homeBackground(): Color {
        composeRule.waitForIdle()
        return composeRule.onRoot().captureToImage().toPixelMap()[1, 1]
    }

    private companion object {
        const val DETAIL = "Detail"
        const val DETAIL_BODY = "detail body"
        const val BACK = "back"
    }
}
