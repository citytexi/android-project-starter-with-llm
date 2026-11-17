package com.example.starter.core.designsystem.theme.dimension

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Corner radius scale. `rN` is N × 4dp, `rNHalf` adds 2dp. [full] is a pill or circle. */
@Immutable
data class StarterRadius(
    val r0Half: Dp,
    val r1: Dp,
    val r1Half: Dp,
    val r2: Dp,
    val r2Half: Dp,
    val r3: Dp,
    val r3Half: Dp,
    val r4: Dp,
    val r5: Dp,
    val r6: Dp,
    val full: Shape,
)

internal val DefaultStarterRadius =
    StarterRadius(
        r0Half = 2.dp,
        r1 = 4.dp,
        r1Half = 6.dp,
        r2 = 8.dp,
        r2Half = 10.dp,
        r3 = 12.dp,
        r3Half = 14.dp,
        r4 = 16.dp,
        r5 = 20.dp,
        r6 = 24.dp,
        full = RoundedCornerShape(percent = 50),
    )
