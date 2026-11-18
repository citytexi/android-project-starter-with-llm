package com.example.starter.core.designsystem.theme

import androidx.compose.ui.graphics.Color
import com.example.starter.core.designsystem.theme.color.Palette

/**
 * Material 3 slots that have no app role token. Each theme picks [Palette] steps for them.
 * When you change the palette, check these too.
 *
 * - [onPrimary]: Material uses `primary` both as a filled background and as text/icon color
 *   (TextButton, Checkbox, progress). So `primary` is `fg.brand`, readable on the surface,
 *   and [onPrimary] is the text on it.
 * - The five surface containers must get steadily darker (light) or lighter (dark): Material
 *   stacks them (a menu over a dialog, a card on a sheet).
 */
internal class MaterialOnlyColors(
    val onPrimary: Color,
    val inversePrimary: Color,
    val surfaceContainerLowest: Color,
    val surfaceContainerLow: Color,
    val surfaceContainer: Color,
    val surfaceContainerHigh: Color,
    val surfaceContainerHighest: Color,
)

internal val LightMaterialOnlyColors =
    MaterialOnlyColors(
        onPrimary = Palette.white,
        inversePrimary = Palette.brand400,
        surfaceContainerLowest = Palette.white,
        surfaceContainerLow = Palette.gray50,
        surfaceContainer = Palette.gray100,
        surfaceContainerHigh = Palette.gray200,
        surfaceContainerHighest = Palette.gray300,
    )

internal val DarkMaterialOnlyColors =
    MaterialOnlyColors(
        onPrimary = Palette.brand950,
        inversePrimary = Palette.brand600,
        surfaceContainerLowest = Palette.gray950,
        surfaceContainerLow = Palette.gray900,
        surfaceContainer = Palette.gray800,
        surfaceContainerHigh = Palette.gray700,
        surfaceContainerHighest = Palette.gray600,
    )
