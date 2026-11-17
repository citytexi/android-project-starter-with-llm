package com.example.starter.core.designsystem.theme.shadow

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** Elevation steps, low to high. Dark uses stronger colors because shadows barely show on dark surfaces. */
@Immutable
data class StarterShadows(
    val s1: StarterShadowToken,
    val s2: StarterShadowToken,
    val s3: StarterShadowToken,
)

internal val LightStarterShadows =
    StarterShadows(
        s1 = StarterShadowToken(0.dp, 1.dp, 4.dp, 0.dp, Color(0x14000000)),
        s2 = StarterShadowToken(0.dp, 2.dp, 10.dp, 0.dp, Color(0x1A000000)),
        s3 = StarterShadowToken(0.dp, 4.dp, 16.dp, 0.dp, Color(0x1F000000)),
    )

internal val DarkStarterShadows =
    StarterShadows(
        s1 = StarterShadowToken(0.dp, 1.dp, 4.dp, 0.dp, Color(0x80000000)),
        s2 = StarterShadowToken(0.dp, 2.dp, 10.dp, 0.dp, Color(0xA6000000)),
        s3 = StarterShadowToken(0.dp, 4.dp, 16.dp, 0.dp, Color(0xCC000000)),
    )
