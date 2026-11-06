package com.example.starter.core.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.starter.core.designsystem.component.StarterButton
import com.example.starter.core.designsystem.theme.StarterTheme
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class StarterButtonScreenshotTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun lightEnabled() = capture("starter_button_light_enabled", darkTheme = false, enabled = true)

    @Test
    fun lightDisabled() = capture("starter_button_light_disabled", darkTheme = false, enabled = false)

    @Test
    fun darkEnabled() = capture("starter_button_dark_enabled", darkTheme = true, enabled = true)

    @Test
    fun darkDisabled() = capture("starter_button_dark_disabled", darkTheme = true, enabled = false)

    private fun capture(
        name: String,
        darkTheme: Boolean,
        enabled: Boolean,
    ) {
        composeRule.setContent { ButtonSample(darkTheme, enabled) }
        composeRule.onRoot().captureRoboImage("build/outputs/roborazzi/$name.png")
    }

    @Composable
    private fun ButtonSample(
        darkTheme: Boolean,
        enabled: Boolean,
    ) {
        StarterTheme(darkTheme = darkTheme) {
            StarterButton(text = "Button", onClick = {}, enabled = enabled)
        }
    }
}
