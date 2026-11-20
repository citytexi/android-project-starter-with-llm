package com.example.starter.core.designsystem.component.button

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.starter.core.designsystem.component.pressable.StarterPressableSurface
import com.example.starter.core.designsystem.theme.StarterPreviewTheme
import com.example.starter.core.designsystem.theme.StarterTheme
import com.example.starter.core.ui.preview.StarterPreview

/**
 * Brand-solid text button on [StarterPressableSurface]: it darkens and shrinks while pressed, with no ripple.
 * The minimum height is the 48dp touch target. Add variants and sizes here as the app needs them.
 */
@Composable
fun StarterButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
) {
    val colors = StarterTheme.colors
    val dimension = StarterTheme.dimension
    val cornerRadius = StarterTheme.radius.r2
    val shape = remember(cornerRadius) { RoundedCornerShape(cornerRadius) }
    val surface = if (enabled) colors.bg.brandSolid else colors.bg.disabled
    StarterPressableSurface(
        color = surface,
        pressedColor = if (enabled) colors.bg.brandSolidPressed else surface,
        shape = shape,
        enabled = enabled,
        onClick = onClick,
        modifier = modifier.defaultMinSize(minHeight = dimension.x12),
        interactionSource = interactionSource,
    ) {
        Text(
            text = text,
            style = StarterTheme.typography.t5Bold,
            color = if (enabled) colors.fg.onBrandSolid else colors.fg.disabled,
            modifier = Modifier.padding(horizontal = dimension.x4, vertical = dimension.x2),
        )
    }
}

@StarterPreview
@Composable
private fun StarterButtonPreview() {
    StarterPreviewTheme {
        Row(
            modifier = Modifier.padding(StarterTheme.dimension.x4),
            horizontalArrangement = Arrangement.spacedBy(StarterTheme.dimension.x2),
        ) {
            StarterButton(text = "Button", onClick = {})
            StarterButton(text = "Button", onClick = {}, enabled = false)
        }
    }
}
