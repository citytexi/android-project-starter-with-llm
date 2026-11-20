package com.example.starter.core.designsystem.component.pressable

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import com.example.starter.core.designsystem.R
import com.example.starter.core.designsystem.theme.StarterTheme

/**
 * Shared surface for interactive components. It owns the pressed color and shrink, the click or toggle, the border,
 * the shadow and the loading semantics, and centers [content].
 *
 * With [selected] null it is a button; otherwise a toggle or a single choice, by [selectionMode]. Selection surfaces have no loading state.
 * [scaleScope] picks whether the whole surface or only [content] shrinks while pressed.
 * [pressedBorder] is drawn over [border] only while pressed and enabled. Keyboard and D-pad focus draws a `stroke.focusRing` ring.
 * [onClickLabel] names the action for TalkBack; button surfaces only, since toggle and selection have no label.
 * Size and accessibility name come in through [modifier]. While loading, [content] draws the progress indicator.
 */
@Composable
internal fun StarterPressableSurface(
    color: Color,
    pressedColor: Color,
    shape: Shape,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    loading: Boolean = false,
    selected: Boolean? = null,
    selectionMode: PressableSelectionMode = PressableSelectionMode.TOGGLE,
    scaleScope: PressScaleScope = PressScaleScope.SELF,
    border: BorderStroke? = null,
    pressedBorder: BorderStroke? = null,
    shadow: Modifier = Modifier,
    onClickLabel: String? = null,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable () -> Unit,
) {
    require(selected == null || (!loading && onClickLabel == null)) { "selection surfaces have no loading state or click label" }
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val loadingDescription = stringResource(R.string.designsystem_loading)
    val scaleSelf = scaleScope == PressScaleScope.SELF
    val surface =
        when {
            selected == null ->
                Modifier.pressable(
                    source,
                    color,
                    pressedColor,
                    shape,
                    enabled,
                    loading,
                    onClick,
                    onClickLabel = onClickLabel,
                    shadow = shadow,
                    scaleSelf = scaleSelf,
                )
            selectionMode == PressableSelectionMode.TOGGLE ->
                Modifier.toggleablePressable(
                    source,
                    selected,
                    color,
                    pressedColor,
                    shape,
                    enabled,
                    onClick,
                    shadow = shadow,
                    scaleSelf = scaleSelf,
                )
            else ->
                Modifier.selectablePressable(
                    source,
                    selected,
                    color,
                    pressedColor,
                    shape,
                    enabled,
                    onClick,
                    shadow = shadow,
                    scaleSelf = scaleSelf,
                )
        }
    val surfaceModifier =
        modifier
            .then(surface)
            .then(FocusRingNode.Element(source, StarterTheme.colors.stroke.focusRing, StarterTheme.dimension.x0Half, shape))
            .then(if (border != null) Modifier.border(border, shape) else Modifier)
            .then(if (pressedBorder != null) pressedBorderModifier(pressedBorder, source, enabled, shape) else Modifier)
            .semantics { if (loading) stateDescription = loadingDescription }
    if (scaleSelf) {
        Box(modifier = surfaceModifier, contentAlignment = Alignment.Center) {
            content()
        }
    } else {
        ContentScaledBox(surfaceModifier, source, enabled, loading, content)
    }
}

// The inner box takes the surface's min constraints, so it is the surface's size and PressScaleNode measures the factor on it.
@Composable
private fun ContentScaledBox(
    surfaceModifier: Modifier,
    source: MutableInteractionSource,
    enabled: Boolean,
    loading: Boolean,
    content: @Composable () -> Unit,
) {
    Box(modifier = surfaceModifier, contentAlignment = Alignment.Center, propagateMinConstraints = true) {
        Box(Modifier.then(PressScaleNode.Element(source, enabled, loading)), contentAlignment = Alignment.Center) {
            content()
        }
    }
}

// Reads the pressed state in composition, so it is only called for surfaces that have a pressedBorder.
@Composable
private fun pressedBorderModifier(
    pressedBorder: BorderStroke,
    source: MutableInteractionSource,
    enabled: Boolean,
    shape: Shape,
): Modifier {
    val pressed by source.collectIsPressedAsState()
    val brush = if (pressed && enabled) pressedBorder.brush else SolidColor(Color.Transparent)
    return Modifier.border(pressedBorder.width, brush, shape)
}
