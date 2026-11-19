package com.example.starter.core.designsystem.component.pressable

import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Keyboard and D-pad focus ring. Taps use `indication = null`, which also drops the default focus highlight,
 * so this draws a [width] ring in [color] along [shape] over the content while [interactionSource] is focused.
 * It sits inside the surface clip and is drawn twice as wide, so [width] shows inside the edge.
 */
internal class FocusRingNode(
    private var interactionSource: InteractionSource,
    private var color: Color,
    private var width: Dp,
    private var shape: Shape,
) : Modifier.Node(),
    DrawModifierNode {
    private var focused = false
    private var focusJob: Job? = null

    override fun onAttach() {
        focused = false
        focusJob = coroutineScope.launchFocusTracking(interactionSource, ::onFocusedChange)
    }

    fun update(
        interactionSource: InteractionSource,
        color: Color,
        width: Dp,
        shape: Shape,
    ) {
        this.color = color
        this.width = width
        this.shape = shape
        if (interactionSource != this.interactionSource) {
            this.interactionSource = interactionSource
            focusJob?.cancel()
            focused = false
            if (isAttached) focusJob = coroutineScope.launchFocusTracking(interactionSource, ::onFocusedChange)
        }
        invalidateDraw()
    }

    private fun onFocusedChange(focused: Boolean) {
        this.focused = focused
        invalidateDraw()
    }

    override fun ContentDrawScope.draw() {
        drawContent()
        if (focused) drawOutline(shape.createOutline(size, layoutDirection, this), color, style = Stroke(width.toPx() * 2))
    }

    data class Element(
        val interactionSource: InteractionSource,
        val color: Color,
        val width: Dp,
        val shape: Shape,
    ) : ModifierNodeElement<FocusRingNode>() {
        override fun create() = FocusRingNode(interactionSource, color, width, shape)

        override fun update(node: FocusRingNode) = node.update(interactionSource, color, width, shape)
    }
}

/** Calls [onChange] whenever [source] gains or loses focus, by the same rule as `collectIsFocusedAsState`. */
private fun CoroutineScope.launchFocusTracking(
    source: InteractionSource,
    onChange: (Boolean) -> Unit,
): Job =
    launch {
        val focuses = mutableListOf<FocusInteraction.Focus>()
        source.interactions.collect { interaction ->
            when (interaction) {
                is FocusInteraction.Focus -> focuses.add(interaction)
                is FocusInteraction.Unfocus -> focuses.remove(interaction.focus)
            }
            onChange(focuses.isNotEmpty())
        }
    }
