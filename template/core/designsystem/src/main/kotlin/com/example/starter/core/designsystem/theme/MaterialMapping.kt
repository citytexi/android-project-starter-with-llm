package com.example.starter.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.ui.graphics.Color
import com.example.starter.core.designsystem.theme.color.DarkColors
import com.example.starter.core.designsystem.theme.color.LightColors
import com.example.starter.core.designsystem.theme.color.StarterColors
import com.example.starter.core.designsystem.theme.dimension.DefaultRadius
import com.example.starter.core.designsystem.theme.dimension.StarterRadius
import com.example.starter.core.designsystem.theme.typography.DefaultTypography
import com.example.starter.core.designsystem.theme.typography.StarterTypography

internal val LightMaterialColorScheme = LightColors.toColorScheme(LightMaterialOnlyColors)
internal val DarkMaterialColorScheme = DarkColors.toColorScheme(DarkMaterialOnlyColors)
internal val DefaultMaterialTypography = DefaultTypography.toMaterialTypography()
internal val DefaultMaterialShapes = DefaultRadius.toMaterialShapes()

/**
 * Bridge so Material 3 components use the app tokens. Every constructor slot is filled by name,
 * so the Material default (purple) never leaks in.
 */
internal fun StarterColors.toColorScheme(materialOnly: MaterialOnlyColors): ColorScheme {
    // Material's fixed roles stay the same in light and dark, so they always come from the light tokens.
    val fixed = LightColors
    return ColorScheme(
        primary = fg.brand,
        onPrimary = materialOnly.onPrimary,
        primaryContainer = bg.brandWeak,
        onPrimaryContainer = fg.brandContrast,
        inversePrimary = materialOnly.inversePrimary,
        secondary = bg.neutralSolid,
        onSecondary = fg.onNeutralSolid,
        // Must differ from surfaceContainer: NavigationBar draws its selected pill in this color on it.
        secondaryContainer = bg.neutralWeakPressed,
        onSecondaryContainer = fg.neutral,
        tertiary = bg.informativeSolid,
        onTertiary = fg.onInformativeSolid,
        tertiaryContainer = bg.informativeWeak,
        onTertiaryContainer = fg.informativeContrast,
        background = bg.layerDefault,
        onBackground = fg.neutral,
        surface = bg.layerDefault,
        onSurface = fg.neutral,
        surfaceVariant = bg.neutralWeak,
        onSurfaceVariant = fg.neutralMuted,
        // With the default (primary), tonal elevation tints surfaces with the brand color.
        surfaceTint = Color.Transparent,
        inverseSurface = bg.neutralInverted,
        inverseOnSurface = fg.neutralInverted,
        error = bg.criticalSolid,
        onError = fg.onCriticalSolid,
        errorContainer = bg.criticalWeak,
        onErrorContainer = fg.criticalContrast,
        outline = stroke.neutralWeak,
        outlineVariant = stroke.neutralMuted,
        scrim = bg.overlay,
        surfaceBright = bg.layerFloating,
        surfaceDim = bg.layerBasement,
        surfaceContainer = materialOnly.surfaceContainer,
        surfaceContainerHigh = materialOnly.surfaceContainerHigh,
        surfaceContainerHighest = materialOnly.surfaceContainerHighest,
        surfaceContainerLow = materialOnly.surfaceContainerLow,
        surfaceContainerLowest = materialOnly.surfaceContainerLowest,
        primaryFixed = fixed.bg.brandWeak,
        primaryFixedDim = fixed.bg.brandWeakPressed,
        onPrimaryFixed = fixed.fg.brandContrast,
        onPrimaryFixedVariant = fixed.fg.brand,
        secondaryFixed = fixed.bg.neutralWeak,
        secondaryFixedDim = fixed.bg.neutralWeakPressed,
        onSecondaryFixed = fixed.fg.neutral,
        onSecondaryFixedVariant = fixed.fg.neutralMuted,
        tertiaryFixed = fixed.bg.informativeWeak,
        tertiaryFixedDim = fixed.bg.informativeWeakPressed,
        onTertiaryFixed = fixed.fg.informativeContrast,
        onTertiaryFixedVariant = fixed.fg.informative,
    )
}

/** Fills all 15 Material 3 text slots from the tokens, so no slot falls back to the Material default. */
internal fun StarterTypography.toMaterialTypography(): Typography =
    Typography(
        displayLarge = t14Bold,
        displayMedium = t13Bold,
        displaySmall = t12Bold,
        headlineLarge = t11Bold,
        headlineMedium = t10Bold,
        headlineSmall = t9Bold,
        titleLarge = t7Bold,
        titleMedium = t5Bold,
        titleSmall = t4Bold,
        bodyLarge = t5Regular,
        bodyMedium = t4Regular,
        bodySmall = t3Regular,
        labelLarge = t4Medium,
        labelMedium = t3Medium,
        labelSmall = t2Medium,
    )

internal fun StarterRadius.toMaterialShapes(): Shapes =
    Shapes(
        extraSmall = RoundedCornerShape(r1),
        small = RoundedCornerShape(r2),
        medium = RoundedCornerShape(r3),
        large = RoundedCornerShape(r4),
        extraLarge = RoundedCornerShape(r6),
    )
