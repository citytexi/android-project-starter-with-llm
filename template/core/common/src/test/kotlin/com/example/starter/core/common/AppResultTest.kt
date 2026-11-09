package com.example.starter.core.common

import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class AppResultTest {
    @Test
    fun `appRunCatching returns Failure on exception`() {
        val boom = IllegalStateException("boom")

        val result = appRunCatching<Int> { throw boom }

        assertIs<AppResult.Failure>(result)
        assertEquals(boom, result.error)
    }

    @Test
    fun `appRunCatching rethrows CancellationException`() {
        assertFailsWith<CancellationException> {
            appRunCatching<Int> { throw CancellationException("cancelled") }
        }
    }
}
