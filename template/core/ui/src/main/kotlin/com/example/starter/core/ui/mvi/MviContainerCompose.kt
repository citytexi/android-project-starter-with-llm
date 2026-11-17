package com.example.starter.core.ui.mvi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle

/** [MviContainer.state] as Compose state, collected only while the screen is at least STARTED. */
@Composable
fun <S : UiState> MviContainer<S, *>.collectAsStateWithLifecycle(): State<S> = state.collectAsStateWithLifecycle()

/**
 * Collects [MviContainer.sideEffect] while the screen is at least STARTED and calls [onSideEffect]
 * for each. Call it once per screen, in the Route. Effects sent while stopped wait in the buffer.
 */
@Suppress("ComposableNaming") // Reads as a call on the container, like collectAsStateWithLifecycle.
@Composable
fun <E : UiSideEffect> MviContainer<*, E>.collectSideEffect(onSideEffect: suspend (E) -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentOnSideEffect by rememberUpdatedState(onSideEffect)
    LaunchedEffect(sideEffect, lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            sideEffect.collect { currentOnSideEffect(it) }
        }
    }
}
