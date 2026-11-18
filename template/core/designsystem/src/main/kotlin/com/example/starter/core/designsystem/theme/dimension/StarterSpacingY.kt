package com.example.starter.core.designsystem.theme.dimension

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp

/** Vertical spacing by role. Each value is a step of the [StarterDimension] scale. */
@Immutable
data class StarterSpacingY(
    val betweenText: Dp,
    val componentDefault: Dp,
    val screenBottom: Dp,
)
