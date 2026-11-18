package com.example.starter.core.designsystem.theme.shadow

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

/** One shadow layer, in CSS box-shadow terms. Draw it with [tokenShadow]. */
@Immutable
data class StarterShadowToken(
    val offsetX: Dp,
    val offsetY: Dp,
    val blur: Dp,
    val spread: Dp,
    val color: Color,
)
