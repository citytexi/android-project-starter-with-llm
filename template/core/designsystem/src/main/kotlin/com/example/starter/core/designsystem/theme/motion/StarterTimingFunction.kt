package com.example.starter.core.designsystem.theme.motion

import androidx.compose.animation.core.Easing
import androidx.compose.runtime.Immutable

/** Easing curves. `enter`/`exit` are for things appearing and leaving, `*Expressive` for larger moves. */
@Immutable
data class StarterTimingFunction(
    val linear: Easing,
    val easing: Easing,
    val enter: Easing,
    val exit: Easing,
    val enterExpressive: Easing,
    val exitExpressive: Easing,
    val pressedScale: Easing,
)
