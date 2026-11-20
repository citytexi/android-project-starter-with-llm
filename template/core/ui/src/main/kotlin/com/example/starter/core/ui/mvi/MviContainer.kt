package com.example.starter.core.ui.mvi

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * What a screen reads from its ViewModel: [state] and [sideEffect]. The ViewModel exposes this
 * read-only view and keeps the [MutableMviContainer] private, like `StateFlow`/`MutableStateFlow`:
 *
 * ```
 * private val _container = MutableMviContainer<FooUiState, FooSideEffect>(viewModelScope, FooUiState())
 * val container: MviContainer<FooUiState, FooSideEffect> = _container
 * ```
 *
 * The Route reads it with `container.collectAsStateWithLifecycle()` and `container.collectSideEffect { }`.
 */
interface MviContainer<S : UiState, E : UiSideEffect> {
    val state: StateFlow<S>

    /**
     * One-off events. **Collect in one place only (the Route)**, with `collectSideEffect`.
     *
     * Backed by a `Channel`, not a `SharedFlow`: an effect sent while nobody collects (during a
     * configuration change) waits in the buffer, and a consumed effect is not replayed when the
     * screen comes back. The cost is a single consumer: two collectors would split the effects.
     */
    val sideEffect: Flow<E>
}
