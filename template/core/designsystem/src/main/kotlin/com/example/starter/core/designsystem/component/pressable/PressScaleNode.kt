package com.example.starter.core.designsystem.component.pressable

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.LayoutModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.currentValueOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Constraints
import com.example.starter.core.designsystem.theme.LocalMotion
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * A layer that shrinks what follows by [pressedScaleFactor] while pressed. The layout size does not change.
 * Nothing shrinks while disabled, loading, or with animations removed.
 */
internal class PressScaleNode(
    private var interactionSource: InteractionSource,
    private var enabled: Boolean,
    private var loading: Boolean,
) : Modifier.Node(),
    LayoutModifierNode,
    CompositionLocalConsumerModifierNode {
    private val shrink = Animatable(0f)
    private var pressed = false
    private var reduceMotion = false
    private var pressJob: Job? = null

    // The layer block reads shrink.value, so an animation redraws the layer without a new layout pass.
    private val layerBlock: GraphicsLayerScope.() -> Unit = {
        val scale = 1f + (pressedScaleFactor(size.width, size.height, density) - 1f) * shrink.value
        scaleX = scale
        scaleY = scale
    }

    override fun onAttach() {
        val reduceMotionFlow = ReduceMotion.flow(currentValueOf(LocalContext))
        reduceMotion = reduceMotionFlow.value
        pressed = false
        if (shrink.value != 0f || shrink.targetValue != 0f) coroutineScope.launch { shrink.snapTo(0f) }
        pressJob = coroutineScope.launchPressTracking(interactionSource, ::onPressedChange)
        coroutineScope.launch {
            reduceMotionFlow.collect {
                reduceMotion = it
                animateToTarget()
            }
        }
    }

    fun update(
        interactionSource: InteractionSource,
        enabled: Boolean,
        loading: Boolean,
    ) {
        this.enabled = enabled
        this.loading = loading
        if (interactionSource != this.interactionSource) {
            this.interactionSource = interactionSource
            pressJob?.cancel()
            pressed = false
            if (isAttached) pressJob = coroutineScope.launchPressTracking(interactionSource, ::onPressedChange)
        }
        if (isAttached) animateToTarget()
    }

    private fun onPressedChange(pressed: Boolean) {
        this.pressed = pressed
        animateToTarget()
    }

    private fun animateToTarget() {
        val canShrink = enabled && !loading && !reduceMotion
        val target = if (canShrink && pressed) 1f else 0f
        if (shrink.targetValue == target) return
        val motion = currentValueOf(LocalMotion)
        coroutineScope.launch {
            shrink.animateTo(target, tween(motion.duration.pressedScale, easing = motion.timingFunction.pressedScale))
        }
    }

    override fun MeasureScope.measure(
        measurable: Measurable,
        constraints: Constraints,
    ): MeasureResult {
        val placeable = measurable.measure(constraints)
        return layout(placeable.width, placeable.height) { placeable.placeWithLayer(0, 0, layerBlock = layerBlock) }
    }

    data class Element(
        val interactionSource: InteractionSource,
        val enabled: Boolean,
        val loading: Boolean,
    ) : ModifierNodeElement<PressScaleNode>() {
        override fun create() = PressScaleNode(interactionSource, enabled, loading)

        override fun update(node: PressScaleNode) = node.update(interactionSource, enabled, loading)
    }
}
