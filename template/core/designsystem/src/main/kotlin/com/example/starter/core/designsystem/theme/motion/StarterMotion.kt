package com.example.starter.core.designsystem.theme.motion

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.runtime.Immutable

/** Motion tokens. Neutral defaults: change them per app. */
@Immutable
data class StarterMotion(
    val duration: StarterDuration,
    val timingFunction: StarterTimingFunction,
)

internal val DefaultMotion =
    StarterMotion(
        duration =
            StarterDuration(
                d1 = 50,
                d2 = 100,
                d3 = 150,
                d4 = 200,
                d5 = 250,
                d6 = 300,
                colorTransition = 150,
                pressedScale = 150,
            ),
        timingFunction =
            StarterTimingFunction(
                linear = CubicBezierEasing(0f, 0f, 1f, 1f),
                easing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f),
                enter = CubicBezierEasing(0f, 0f, 0.2f, 1f),
                exit = CubicBezierEasing(0.4f, 0f, 1f, 1f),
                enterExpressive = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f),
                exitExpressive = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f),
                pressedScale = CubicBezierEasing(0f, 0f, 0.2f, 1f),
            ),
    )
