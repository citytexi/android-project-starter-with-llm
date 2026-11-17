package com.example.starter.core.common

import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

class RunSuspendCatchingTest {
    @Test
    fun `returns success with the value`() =
        runTest {
            val result =
                runSuspendCatching {
                    delay(1)
                    42
                }

            assertEquals(42, result.getOrThrow())
        }

    @Test
    fun `returns failure on exception`() {
        val boom = IllegalStateException("boom")

        val result = runSuspendCatching<Int> { throw boom }

        assertSame(boom, result.exceptionOrNull())
    }

    @Test
    fun `rethrows CancellationException`() {
        assertFailsWith<CancellationException> {
            runSuspendCatching<Int> { throw CancellationException("cancelled") }
        }
    }
}
