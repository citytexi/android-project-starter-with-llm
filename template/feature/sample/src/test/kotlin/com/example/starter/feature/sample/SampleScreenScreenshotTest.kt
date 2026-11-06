package com.example.starter.feature.sample

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.starter.core.designsystem.theme.StarterTheme
import com.example.starter.domain.model.SampleItem
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode

/** Screenshots of the stateless [SampleScreen] only. `SampleRoute` needs Hilt and is not captured. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SampleScreenScreenshotTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun empty() = capture("sample_screen_empty", SampleUiState())

    @Test
    fun items() =
        capture(
            "sample_screen_items",
            SampleUiState(items = listOf(SampleItem(1, "First"), SampleItem(2, "Second"), SampleItem(3, "Third"))),
        )

    @Test
    fun refreshing() = capture("sample_screen_refreshing", SampleUiState(isRefreshing = true))

    @Test
    fun error() = capture("sample_screen_error", SampleUiState(errorMessage = "Unable to resolve host"))

    private fun capture(
        name: String,
        uiState: SampleUiState,
    ) {
        composeRule.setContent {
            StarterTheme(darkTheme = false) {
                SampleScreen(uiState = uiState, onAction = {})
            }
        }
        composeRule.onRoot().captureRoboImage("build/outputs/roborazzi/$name.png")
    }
}
