package com.example.starter.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import com.example.starter.core.designsystem.theme.color.DarkColors
import com.example.starter.core.designsystem.theme.color.LightColors
import com.example.starter.core.designsystem.theme.color.StarterColors
import com.example.starter.core.designsystem.theme.dimension.DefaultDimension
import com.example.starter.core.designsystem.theme.dimension.DefaultRadius
import com.example.starter.core.designsystem.theme.dimension.StarterDimension
import com.example.starter.core.designsystem.theme.dimension.StarterRadius
import com.example.starter.core.designsystem.theme.motion.DefaultMotion
import com.example.starter.core.designsystem.theme.motion.StarterMotion
import com.example.starter.core.designsystem.theme.shadow.DarkShadows
import com.example.starter.core.designsystem.theme.shadow.LightShadows
import com.example.starter.core.designsystem.theme.shadow.StarterShadows
import com.example.starter.core.designsystem.theme.typography.DefaultTypography
import com.example.starter.core.designsystem.theme.typography.StarterTypography

// Defaults are light, so previews and tests not wrapped in StarterTheme still draw light values.
internal val LocalColors = staticCompositionLocalOf { LightColors }
internal val LocalTypography = staticCompositionLocalOf { DefaultTypography }
internal val LocalDimension = staticCompositionLocalOf { DefaultDimension }
internal val LocalRadius = staticCompositionLocalOf { DefaultRadius }
internal val LocalShadows = staticCompositionLocalOf { LightShadows }
internal val LocalMotion = staticCompositionLocalOf { DefaultMotion }

/** Provides the app tokens and wraps Material 3 in the same values. */
@Composable
fun StarterTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DarkColors else LightColors
    val shadows = if (darkTheme) DarkShadows else LightShadows
    CompositionLocalProvider(
        LocalColors provides colors,
        LocalTypography provides DefaultTypography,
        LocalDimension provides DefaultDimension,
        LocalRadius provides DefaultRadius,
        LocalShadows provides shadows,
        LocalMotion provides DefaultMotion,
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkMaterialColorScheme else LightMaterialColorScheme,
            typography = DefaultMaterialTypography,
            shapes = DefaultMaterialShapes,
            content = content,
        )
    }
}

/** Where screens read tokens, like `MaterialTheme`: `StarterTheme.colors.bg.brandSolid`. */
object StarterTheme {
    val colors: StarterColors
        @Composable @ReadOnlyComposable
        get() = LocalColors.current

    val typography: StarterTypography
        @Composable @ReadOnlyComposable
        get() = LocalTypography.current

    val dimension: StarterDimension
        @Composable @ReadOnlyComposable
        get() = LocalDimension.current

    val radius: StarterRadius
        @Composable @ReadOnlyComposable
        get() = LocalRadius.current

    val shadows: StarterShadows
        @Composable @ReadOnlyComposable
        get() = LocalShadows.current

    val motion: StarterMotion
        @Composable @ReadOnlyComposable
        get() = LocalMotion.current
}
