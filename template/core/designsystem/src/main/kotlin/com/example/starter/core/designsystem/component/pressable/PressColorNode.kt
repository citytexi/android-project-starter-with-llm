package com.example.starter.core.designsystem.component.pressable

import androidx.compose.animation.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.currentValueOf
import com.example.starter.core.designsystem.theme.LocalMotion
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Surface background. Turns [pressedColor] while pressed, and stays [pressedColor] while loading whether pressed or not.
 * Text and icon colors are left alone. A node reused for another item (a Lazy list scrolling) jumps to the new color
 * instead of fading from the previous item's.
 */
internal class PressColorNode(
    private var interactionSource: InteractionSource,
    private var color: Color,
    private var pressedColor: Color,
    private var enabled: Boolean,
    private var loading: Boolean,
) : Modifier.Node(),
    DrawModifierNode,
    CompositionLocalConsumerModifierNode {
    private var pressed = false
    private val background = Animatable(target())
    private var pressJob: Job? = null

    // Set by onReset. On reuse onAttach runs with the old item's colors and update() brings the new ones right after.
    private var snapOnNextUpdate = false

    override fun onReset() {
        snapOnNextUpdate = true
    }

    override fun onAttach() {
        pressed = false
        val initial = target()
        if (background.value != initial || background.targetValue != initial) coroutineScope.launch { background.snapTo(initial) }
        pressJob = coroutineScope.launchPressTracking(interactionSource, ::onPressedChange)
    }

    fun update(
        interactionSource: InteractionSource,
        color: Color,
        pressedColor: Color,
        enabled: Boolean,
        loading: Boolean,
    ) {
        this.color = color
        this.pressedColor = pressedColor
        this.enabled = enabled
        this.loading = loading
        if (interactionSource != this.interactionSource) {
            this.interactionSource = interactionSource
            pressJob?.cancel()
            pressed = false
            if (isAttached) pressJob = coroutineScope.launchPressTracking(interactionSource, ::onPressedChange)
        }
        if (!isAttached) return
        if (snapOnNextUpdate) {
            snapOnNextUpdate = false
            val target = target()
            coroutineScope.launch { background.snapTo(target) }
        } else {
            animateToTarget()
        }
    }

    private fun target(): Color = if (loading || (enabled && pressed)) pressedColor else color

    private fun onPressedChange(pressed: Boolean) {
        snapOnNextUpdate = false
        this.pressed = pressed
        animateToTarget()
    }

    private fun animateToTarget() {
        val target = target()
        if (background.targetValue == target) return
        val motion = currentValueOf(LocalMotion)
        coroutineScope.launch {
            background.animateTo(target, tween(motion.duration.colorTransition, easing = motion.timingFunction.easing))
        }
    }

    override fun ContentDrawScope.draw() {
        drawRect(background.value)
        drawContent()
    }

    data class Element(
        val interactionSource: InteractionSource,
        val color: Color,
        val pressedColor: Color,
        val enabled: Boolean,
        val loading: Boolean,
    ) : ModifierNodeElement<PressColorNode>() {
        override fun create() = PressColorNode(interactionSource, color, pressedColor, enabled, loading)

        override fun update(node: PressColorNode) = node.update(interactionSource, color, pressedColor, enabled, loading)
    }
}
