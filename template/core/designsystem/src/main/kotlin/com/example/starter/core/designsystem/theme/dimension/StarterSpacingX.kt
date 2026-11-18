package com.example.starter.core.designsystem.theme.dimension

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp

/** Horizontal spacing by role. Each value is a step of the [StarterDimension] scale. */
@Immutable
data class StarterSpacingX(
    val globalGutter: Dp,
    val betweenChips: Dp,
)
