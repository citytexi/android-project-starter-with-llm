package com.example.starter.core.ui.mvi

import app.cash.turbine.test
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class MviContainerTest {
    private data class CounterState(
        val count: Int = 0,
    ) : UiState

    private data class Toast(
        val text: String,
    ) : UiSideEffect

    private fun TestScope.container() =
        MutableMviContainer<CounterState, Toast>(
            // Unconfined: launched work runs at once, like viewModelScope on Main.immediate.
            CoroutineScope(backgroundScope.coroutineContext + UnconfinedTestDispatcher(testScheduler)),
            CounterState(),
        )

    @Test
    fun `updateState applies the reducer to the current state`() =
        runTest {
            val container = container()
            container.updateState { copy(count = count + 1) }
            container.updateState { copy(count = count + 1) }
            assertEquals(2, container.state.value.count)
        }

    @Test
    fun `side effects posted before collection are delivered once`() =
        runTest {
            val container = container()
            assertTrue(container.postSideEffect(Toast("a")))
            assertTrue(container.postSideEffect(Toast("b")))

            container.sideEffect.test {
                assertEquals(Toast("a"), awaitItem())
                assertEquals(Toast("b"), awaitItem())
            }
            // Consumed effects are not replayed to a new collector.
            container.sideEffect.test { expectNoEvents() }
        }

    @Test
    fun `postSideEffect returns false when the buffer is full`() =
        runTest {
            val container = container()
            var sent = 0
            while (container.postSideEffect(Toast("x"))) sent++
            assertEquals(64, sent)
        }

    @Test
    fun `launch with a running key is ignored`() =
        runTest {
            val container = container()
            val gate = CompletableDeferred<Unit>()
            var runs = 0

            val first =
                container.launch(key = "k") {
                    runs++
                    gate.await()
                }
            val second = container.launch(key = "k") { runs++ }

            assertNotNull(first)
            assertNull(second)
            gate.complete(Unit)
            runCurrent()
            assertNotNull(container.launch(key = "k") { runs++ })
            assertEquals(2, runs)
        }

    @Test
    fun `launch without a key always runs`() =
        runTest {
            val container = container()
            var runs = 0
            repeat(3) { container.launch { runs++ } }
            assertEquals(3, runs)
        }

    @Test
    fun `onError receives the exception and the scope survives`() =
        runTest {
            val container = container()
            var caught: Throwable? = null

            container.launch(onError = { caught = it }) { error("boom") }

            assertEquals("boom", caught?.message)
            var ranAfter = false
            container.launch { ranAfter = true }
            assertTrue(ranAfter)
        }

    @Test
    fun `launchWhileSubscribed collects only while state has a collector`() =
        runTest {
            val container = container()
            val source = MutableStateFlow(0)
            var open = false
            val seen = mutableListOf<Int>()
            container.launchWhileSubscribed(
                stopTimeout = 5.seconds,
                source = { source.onStart { open = true }.onCompletion { open = false } },
            ) { seen += it }
            assertFalse(open)

            val collector = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { container.state.collect {} }
            assertTrue(open)
            source.value = 1

            collector.cancel()
            advanceTimeBy(4.seconds)
            assertTrue(open, "kept open during the grace period")
            advanceTimeBy(2.seconds)
            assertFalse(open)
            assertEquals(listOf(0, 1), seen)
        }
}
