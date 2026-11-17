package com.example.starter.domain

import com.example.starter.domain.testing.FakeSampleRepository
import com.example.starter.domain.usecase.RefreshSampleItemsUseCase
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

class RefreshSampleItemsUseCaseTest {
    @Test
    fun `invoke returns success when the repository refreshes`() =
        runTest {
            val repository = FakeSampleRepository()

            val result = RefreshSampleItemsUseCase(repository)()

            assertTrue(result.isSuccess)
            assertEquals(1, repository.refreshCount)
        }

    @Test
    fun `invoke wraps a repository exception into failure`() =
        runTest {
            val failure = IllegalStateException("offline")
            val repository = FakeSampleRepository().apply { refreshError = failure }

            val result = RefreshSampleItemsUseCase(repository)()

            assertSame(failure, result.exceptionOrNull())
        }
}
