package com.example.starter.domain

import com.example.starter.core.common.AppResult
import com.example.starter.domain.testing.FakeSampleRepository
import com.example.starter.domain.usecase.RefreshSampleItemsUseCase
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class RefreshSampleItemsUseCaseTest {
    @Test
    fun `invoke delegates to repository refresh`() =
        runTest {
            val failure = AppResult.Failure(IllegalStateException("offline"))
            val repository = FakeSampleRepository().apply { refreshResult = failure }

            val result = RefreshSampleItemsUseCase(repository)()

            assertEquals(failure, result)
            assertEquals(1, repository.refreshCount)
        }
}
