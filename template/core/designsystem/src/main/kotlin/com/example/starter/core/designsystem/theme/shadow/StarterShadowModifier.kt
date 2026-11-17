package com.example.starter.core.designsystem.theme.shadow

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.unit.DpOffset

/** Draws a shadow token with `dropShadow`. Put it before `background` so the shadow sits behind the surface. */
fun Modifier.starterShadow(
    token: StarterShadowToken,
    shape: Shape,
): Modifier =
    dropShadow(
        shape,
        Shadow(
            radius = token.blur,
            spread = token.spread,
            offset = DpOffset(token.offsetX, token.offsetY),
            color = token.color,
        ),
    )
