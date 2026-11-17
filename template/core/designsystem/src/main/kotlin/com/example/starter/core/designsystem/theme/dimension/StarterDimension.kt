package com.example.starter.core.designsystem.theme.dimension

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Spacing scale on a 4dp grid. `xN` is N × 4dp, `xNHalf` adds 2dp. Neutral defaults: change them per app. */
@Immutable
data class StarterDimension(
    val x0Half: Dp,
    val x1: Dp,
    val x1Half: Dp,
    val x2: Dp,
    val x2Half: Dp,
    val x3: Dp,
    val x3Half: Dp,
    val x4: Dp,
    val x4Half: Dp,
    val x5: Dp,
    val x6: Dp,
    val x7: Dp,
    val x8: Dp,
    val x9: Dp,
    val x10: Dp,
    val x12: Dp,
    val x14: Dp,
    val x16: Dp,
    val spacingX: StarterSpacingX,
    val spacingY: StarterSpacingY,
)

internal val DefaultStarterDimension =
    StarterDimension(
        x0Half = 2.dp,
        x1 = 4.dp,
        x1Half = 6.dp,
        x2 = 8.dp,
        x2Half = 10.dp,
        x3 = 12.dp,
        x3Half = 14.dp,
        x4 = 16.dp,
        x4Half = 18.dp,
        x5 = 20.dp,
        x6 = 24.dp,
        x7 = 28.dp,
        x8 = 32.dp,
        x9 = 36.dp,
        x10 = 40.dp,
        x12 = 48.dp,
        x14 = 56.dp,
        x16 = 64.dp,
        spacingX =
            StarterSpacingX(
                globalGutter = 16.dp,
                betweenChips = 8.dp,
            ),
        spacingY =
            StarterSpacingY(
                betweenText = 6.dp,
                componentDefault = 12.dp,
                screenBottom = 56.dp,
            ),
    )
