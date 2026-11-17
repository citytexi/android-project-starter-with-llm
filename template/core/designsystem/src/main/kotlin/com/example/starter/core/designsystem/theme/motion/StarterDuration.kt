package com.example.starter.core.designsystem.theme.motion

import androidx.compose.runtime.Immutable

/** Animation lengths in milliseconds. `d1`..`d6` is the scale, the rest are roles. */
@Immutable
data class StarterDuration(
    val d1: Int,
    val d2: Int,
    val d3: Int,
    val d4: Int,
    val d5: Int,
    val d6: Int,
    val colorTransition: Int,
    val pressedScale: Int,
)
