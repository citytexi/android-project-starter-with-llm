package com.example.starter.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.ui.graphics.Color
import com.example.starter.core.designsystem.theme.color.DarkStarterColors
import com.example.starter.core.designsystem.theme.color.LightStarterColors
import com.example.starter.core.designsystem.theme.color.Palette
import com.example.starter.core.designsystem.theme.color.StarterColors
import com.example.starter.core.designsystem.theme.dimension.DefaultStarterRadius
import com.example.starter.core.designsystem.theme.dimension.StarterRadius
import com.example.starter.core.designsystem.theme.typography.DefaultStarterTypography
import com.example.starter.core.designsystem.theme.typography.StarterTypography

internal val LightMaterialColorScheme = LightStarterColors.toColorScheme(inversePrimary = Palette.brand400)
internal val DarkMaterialColorScheme = DarkStarterColors.toColorScheme(inversePrimary = Palette.brand600)
internal val DefaultMaterialTypography = DefaultStarterTypography.toMaterialTypography()
internal val DefaultMaterialShapes = DefaultStarterRadius.toMaterialShapes()

/**
 * Bridge so Material 3 components use the app tokens. Every constructor slot is filled by name,
 * so the Material default (purple) never leaks in. `inversePrimary` has no role token, so each
 * theme passes a palette step.
 */
internal fun StarterColors.toColorScheme(inversePrimary: Color): ColorScheme =
    ColorScheme(
        primary = bg.brandSolid,
        onPrimary = fg.onBrandSolid,
        primaryContainer = bg.brandWeak,
        onPrimaryContainer = fg.brandContrast,
        inversePrimary = inversePrimary,
        secondary = bg.neutralSolid,
        onSecondary = fg.onNeutralSolid,
        secondaryContainer = bg.neutralWeak,
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
        surfaceContainer = bg.neutralMuted,
        surfaceContainerHigh = bg.layerFloating,
        surfaceContainerHighest = bg.neutralWeak,
        surfaceContainerLow = bg.layerFloating,
        surfaceContainerLowest = bg.layerDefault,
        primaryFixed = bg.brandWeak,
        primaryFixedDim = bg.brandWeakPressed,
        onPrimaryFixed = fg.brandContrast,
        onPrimaryFixedVariant = fg.brand,
        secondaryFixed = bg.neutralWeak,
        secondaryFixedDim = bg.neutralWeakPressed,
        onSecondaryFixed = fg.neutral,
        onSecondaryFixedVariant = fg.neutralMuted,
        tertiaryFixed = bg.informativeWeak,
        tertiaryFixedDim = bg.informativeWeakPressed,
        onTertiaryFixed = fg.informativeContrast,
        onTertiaryFixedVariant = fg.informative,
    )

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
