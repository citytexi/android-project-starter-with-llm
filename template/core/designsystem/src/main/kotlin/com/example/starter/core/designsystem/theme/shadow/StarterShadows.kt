package com.example.starter.core.designsystem.theme.shadow

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.dp
import com.example.starter.core.designsystem.theme.color.Palette

/** Elevation steps, low to high. Dark uses stronger colors because shadows barely show on dark surfaces. */
@Immutable
data class StarterShadows(
    val s1: StarterShadowToken,
    val s2: StarterShadowToken,
    val s3: StarterShadowToken,
)

internal val LightShadows =
    StarterShadows(
        s1 = StarterShadowToken(0.dp, 1.dp, 4.dp, 0.dp, Palette.blackAlpha8),
        s2 = StarterShadowToken(0.dp, 2.dp, 10.dp, 0.dp, Palette.blackAlpha10),
        s3 = StarterShadowToken(0.dp, 4.dp, 16.dp, 0.dp, Palette.blackAlpha12),
    )

internal val DarkShadows =
    StarterShadows(
        s1 = StarterShadowToken(0.dp, 1.dp, 4.dp, 0.dp, Palette.blackAlpha50),
        s2 = StarterShadowToken(0.dp, 2.dp, 10.dp, 0.dp, Palette.blackAlpha65),
        s3 = StarterShadowToken(0.dp, 4.dp, 16.dp, 0.dp, Palette.blackAlpha80),
    )
