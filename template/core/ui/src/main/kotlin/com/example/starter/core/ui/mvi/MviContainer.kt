package com.example.starter.core.ui.mvi

import com.example.starter.core.common.runSuspendCatching
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

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

/**
 * The ViewModel's side of [MviContainer]: changes state, posts effects and runs work. A ViewModel
 * owns one instead of extending a base class. Plain Kotlin, so it is unit-tested on the JVM.
 *
 * @param scope where [launch] and [launchWhileSubscribed] run, normally `viewModelScope`.
 */
class MutableMviContainer<S : UiState, E : UiSideEffect>(
    private val scope: CoroutineScope,
    initialState: S,
) : MviContainer<S, E> {
    private val mutableState = MutableStateFlow(initialState)

    override val state: StateFlow<S> = mutableState.asStateFlow()

    private val effects = Channel<E>(Channel.BUFFERED)

    override val sideEffect: Flow<E> = effects.receiveAsFlow()

    /** Replaces the state with `reducer(current)`. Atomic, safe from any thread. */
    fun updateState(reducer: S.() -> S) {
        mutableState.update { it.reducer() }
    }

    /**
     * Sends [effect] without suspending. Returns `false` if the buffer (64 effects nobody
     * collected) is full and the effect was dropped.
     */
    fun postSideEffect(effect: E): Boolean = effects.trySend(effect).isSuccess

    private val lock = Any()
    private val runningJobs = mutableMapOf<Any, Job>()

    /**
     * Runs [block] in [scope].
     *
     * @param key while a job with the same key is still active, a new call does nothing and
     *   returns `null` (for example a double-tapped button). `null` disables the check.
     * @param onError called with an unexpected exception thrown by [block]. Use it to post an
     *   error effect. Without it the exception propagates as in a plain `scope.launch`.
     *   A UseCase's `Result.failure` is a value, not an exception: handle it in [block].
     */
    fun launch(
        key: Any? = null,
        onError: ((Throwable) -> Unit)? = null,
        block: suspend CoroutineScope.() -> Unit,
    ): Job? {
        val job =
            scope.launch(start = CoroutineStart.LAZY) {
                if (onError == null) {
                    block()
                    return@launch
                }
                // coroutineScope rethrows a failed child's own exception here instead of cancelling
                // this coroutine first. runSuspendCatching rethrows CancellationException.
                runSuspendCatching { coroutineScope { block() } }.onFailure(onError)
            }
        if (key != null) {
            synchronized(lock) {
                if (runningJobs[key]?.isActive == true) {
                    job.cancel()
                    return null
                }
                runningJobs[key] = job
            }
            job.invokeOnCompletion {
                synchronized(lock) { if (runningJobs[key] === job) runningJobs.remove(key) }
            }
        }
        // Started only after it is registered, so a job that finishes at once is removed.
        job.start()
        return job
    }

    /**
     * Collects [source] only while the screen is visible, that is while [state] has a collector
     * (the Route collects it with `container.collectAsStateWithLifecycle()`). Use it for observing data
     * instead of `stateIn(WhileSubscribed)`; a [launch] job keeps running in the back stack.
     *
     * Do not collect [state] inside [source]: it would count as a collector and keep itself open.
     *
     * @param stopTimeout grace period after the last collector leaves, so a configuration change
     *   or a quick screen switch does not restart [source].
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun <T> launchWhileSubscribed(
        stopTimeout: Duration = DEFAULT_STOP_TIMEOUT,
        source: () -> Flow<T>,
        collector: suspend (T) -> Unit,
    ): Job =
        scope.launch {
            mutableState.subscriptionCount
                .map { it > 0 }
                .distinctUntilChanged()
                .flatMapLatest { subscribed ->
                    if (subscribed) {
                        flowOf(true)
                    } else {
                        flow {
                            delay(stopTimeout)
                            emit(false)
                        }
                    }
                }.distinctUntilChanged()
                .flatMapLatest { active -> if (active) source() else emptyFlow() }
                .collect(collector)
        }

    private companion object {
        val DEFAULT_STOP_TIMEOUT: Duration = 5.seconds
    }
}
