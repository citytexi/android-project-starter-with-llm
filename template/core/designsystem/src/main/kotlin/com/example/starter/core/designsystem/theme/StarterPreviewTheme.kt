package com.example.starter.core.designsystem.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier

/**
 * Wraps the body of a `@StarterPreview`. On top of [StarterTheme] it paints the screen background
 * (`bg.layerDefault`) and sets the default text color (`fg.neutral`). `showBackground` is always white,
 * so without this a Dark preview draws light text on white.
 *
 * Previews only. Screens and components use [StarterTheme]. Konsist `PreviewThemeRulesTest` enforces both.
 */
@Composable
fun StarterPreviewTheme(content: @Composable () -> Unit) {
    StarterTheme {
        CompositionLocalProvider(LocalContentColor provides StarterTheme.colors.fg.neutral) {
            Box(Modifier.background(StarterTheme.colors.bg.layerDefault)) {
                content()
            }
        }
    }
}
