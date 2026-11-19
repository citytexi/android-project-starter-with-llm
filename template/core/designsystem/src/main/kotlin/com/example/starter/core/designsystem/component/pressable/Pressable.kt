package com.example.starter.core.designsystem.component.pressable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.max

private const val PRESSED_SHRINK_DP = 2f
private const val MIN_REFERENCE_DP = 24f
private const val WIDTH_DIVISOR = 4f

/** Scale while pressed: the surface loses 2dp on its reference length, the largest of height, width / 4 and 24dp. */
internal fun pressedScaleFactor(
    widthPx: Float,
    heightPx: Float,
    density: Float,
): Float {
    val reference = max(heightPx, max(widthPx / WIDTH_DIVISOR, MIN_REFERENCE_DP * density))
    return (reference - PRESSED_SHRINK_DP * density) / reference
}

/**
 * While pressed the background turns [pressedColor] and the surface shrinks by [pressedScaleFactor]. No ripple.
 * [shadow] is drawn outside the clip and shrinks with the surface; a shadow placed before this modifier does not shrink.
 * With [scaleSelf] false the surface does not shrink, for surfaces that shrink only their content.
 * [onClickLabel] is what TalkBack reads for the action ("double tap to [onClickLabel]").
 */
internal fun Modifier.pressable(
    interactionSource: MutableInteractionSource,
    color: Color,
    pressedColor: Color,
    shape: Shape,
    enabled: Boolean,
    loading: Boolean,
    onClick: () -> Unit,
    role: Role = Role.Button,
    onClickLabel: String? = null,
    shadow: Modifier = Modifier,
    scaleSelf: Boolean = true,
): Modifier =
    clickable(
        interactionSource = interactionSource,
        indication = null,
        enabled = enabled,
        onClickLabel = onClickLabel,
        role = role,
        onClick = { if (!loading) onClick() },
    ).pressableSurface(interactionSource, color, pressedColor, shape, enabled, loading, shadow, scaleSelf)

/** Like [pressable], with an on/off state. The default [role] is a chip's. */
internal fun Modifier.toggleablePressable(
    interactionSource: MutableInteractionSource,
    selected: Boolean,
    color: Color,
    pressedColor: Color,
    shape: Shape,
    enabled: Boolean,
    onClick: () -> Unit,
    role: Role = Role.Checkbox,
    shadow: Modifier = Modifier,
    scaleSelf: Boolean = true,
): Modifier =
    toggleable(
        value = selected,
        interactionSource = interactionSource,
        indication = null,
        enabled = enabled,
        role = role,
        onValueChange = { onClick() },
    ).pressableSurface(interactionSource, color, pressedColor, shape, enabled, loading = false, shadow = shadow, scaleSelf = scaleSelf)

/** Like [toggleablePressable], for picking one of several. Pressing the selected item still calls [onClick]. */
internal fun Modifier.selectablePressable(
    interactionSource: MutableInteractionSource,
    selected: Boolean,
    color: Color,
    pressedColor: Color,
    shape: Shape,
    enabled: Boolean,
    onClick: () -> Unit,
    shadow: Modifier = Modifier,
    scaleSelf: Boolean = true,
): Modifier =
    selectable(
        selected = selected,
        interactionSource = interactionSource,
        indication = null,
        enabled = enabled,
        role = Role.RadioButton,
        onClick = onClick,
    ).pressableSurface(interactionSource, color, pressedColor, shape, enabled, loading = false, shadow = shadow, scaleSelf = scaleSelf)

// The tap target stays outside the scale layer. If it shrank too, a finger on the edge would end up outside the
// shrunk surface and the tap would be cancelled. The scale layer comes first inside, so shadow, clip and background shrink together.
private fun Modifier.pressableSurface(
    interactionSource: MutableInteractionSource,
    color: Color,
    pressedColor: Color,
    shape: Shape,
    enabled: Boolean,
    loading: Boolean,
    shadow: Modifier,
    scaleSelf: Boolean,
): Modifier =
    this
        .then(if (scaleSelf) PressScaleNode.Element(interactionSource, enabled, loading) else Modifier)
        .then(shadow)
        .clip(shape)
        .then(PressColorNode.Element(interactionSource, color, pressedColor, enabled, loading))

/** Calls [onChange] whenever [source] starts or stops being pressed, by the same rule as `collectIsPressedAsState`. */
internal fun CoroutineScope.launchPressTracking(
    source: InteractionSource,
    onChange: (Boolean) -> Unit,
): Job =
    launch {
        val presses = mutableListOf<PressInteraction.Press>()
        source.interactions.collect { interaction ->
            when (interaction) {
                is PressInteraction.Press -> presses.add(interaction)
                is PressInteraction.Release -> presses.remove(interaction.press)
                is PressInteraction.Cancel -> presses.remove(interaction.press)
            }
            onChange(presses.isNotEmpty())
        }
    }
