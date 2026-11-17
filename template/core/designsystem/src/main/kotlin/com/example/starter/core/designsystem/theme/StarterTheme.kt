package com.example.starter.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import com.example.starter.core.designsystem.theme.color.DarkStarterColors
import com.example.starter.core.designsystem.theme.color.LightStarterColors
import com.example.starter.core.designsystem.theme.color.StarterColors
import com.example.starter.core.designsystem.theme.dimension.DefaultStarterDimension
import com.example.starter.core.designsystem.theme.dimension.DefaultStarterRadius
import com.example.starter.core.designsystem.theme.dimension.StarterDimension
import com.example.starter.core.designsystem.theme.dimension.StarterRadius
import com.example.starter.core.designsystem.theme.motion.DefaultStarterMotion
import com.example.starter.core.designsystem.theme.motion.StarterMotion
import com.example.starter.core.designsystem.theme.shadow.DarkStarterShadows
import com.example.starter.core.designsystem.theme.shadow.LightStarterShadows
import com.example.starter.core.designsystem.theme.shadow.StarterShadows
import com.example.starter.core.designsystem.theme.typography.DefaultStarterTypography
import com.example.starter.core.designsystem.theme.typography.StarterTypography

// Defaults are light, so previews and tests not wrapped in StarterTheme still draw light values.
internal val LocalStarterColors = staticCompositionLocalOf { LightStarterColors }
internal val LocalStarterTypography = staticCompositionLocalOf { DefaultStarterTypography }
internal val LocalStarterDimension = staticCompositionLocalOf { DefaultStarterDimension }
internal val LocalStarterRadius = staticCompositionLocalOf { DefaultStarterRadius }
internal val LocalStarterShadows = staticCompositionLocalOf { LightStarterShadows }
internal val LocalStarterMotion = staticCompositionLocalOf { DefaultStarterMotion }

/** Provides the app tokens and wraps Material 3 in the same values. */
@Composable
fun StarterTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DarkStarterColors else LightStarterColors
    val shadows = if (darkTheme) DarkStarterShadows else LightStarterShadows
    CompositionLocalProvider(
        LocalStarterColors provides colors,
        LocalStarterTypography provides DefaultStarterTypography,
        LocalStarterDimension provides DefaultStarterDimension,
        LocalStarterRadius provides DefaultStarterRadius,
        LocalStarterShadows provides shadows,
        LocalStarterMotion provides DefaultStarterMotion,
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkMaterialColorScheme else LightMaterialColorScheme,
            typography = DefaultMaterialTypography,
            shapes = DefaultMaterialShapes,
            content = content,
        )
    }
}

/** Where screens read tokens, like `MaterialTheme`: `StarterTheme.color.bg.brandSolid`. */
object StarterTheme {
    val color: StarterColors
        @Composable @ReadOnlyComposable
        get() = LocalStarterColors.current

    val typography: StarterTypography
        @Composable @ReadOnlyComposable
        get() = LocalStarterTypography.current

    val dimension: StarterDimension
        @Composable @ReadOnlyComposable
        get() = LocalStarterDimension.current

    val radius: StarterRadius
        @Composable @ReadOnlyComposable
        get() = LocalStarterRadius.current

    val shadow: StarterShadows
        @Composable @ReadOnlyComposable
        get() = LocalStarterShadows.current

    val motion: StarterMotion
        @Composable @ReadOnlyComposable
        get() = LocalStarterMotion.current
}
