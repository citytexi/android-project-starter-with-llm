package com.example.starter.feature.sample

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.res.stringResource
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

    // First load: nothing cached yet, so a centered spinner stands in for the list.
    @Test
    fun refreshing() = capture("sample_screen_refreshing", SampleUiState(isRefreshing = true))

    // Refresh with cached items: the list stays and a bar shows on top.
    @Test
    fun refreshingWithItems() =
        capture(
            "sample_screen_refreshing_with_items",
            SampleUiState(items = listOf(SampleItem(1, "First"), SampleItem(2, "Second")), isRefreshing = true),
        )

    // Errors are SampleSideEffect.ShowError: the Route shows them in a snackbar, as here.
    @Test
    fun networkError() = capture("sample_screen_error_network", SampleUiState(), SampleError.Network)

    @Test
    fun serverError() = capture("sample_screen_error_server", SampleUiState(), SampleError.Server)

    @Test
    fun unknownError() =
        capture(
            "sample_screen_error_unknown",
            SampleUiState(items = listOf(SampleItem(1, "First"))),
            SampleError.Unknown,
        )

    private fun capture(
        name: String,
        state: SampleUiState,
        error: SampleError? = null,
    ) {
        val snackbarHostState = SnackbarHostState()
        composeRule.setContent {
            StarterTheme(darkTheme = false) {
                SampleScreen(state = state, snackbarHostState = snackbarHostState, onIntent = {})
            }
            if (error != null) {
                val message = stringResource(error.messageRes)
                // Indefinite: the snackbar stays until captured (a timed one would be gone once idle).
                LaunchedEffect(message) {
                    snackbarHostState.showSnackbar(message, withDismissAction = true, duration = SnackbarDuration.Indefinite)
                }
            }
        }
        composeRule.onRoot().captureRoboImage("build/outputs/roborazzi/$name.png")
    }
}
