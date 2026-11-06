package com.example.starter.domain

import com.example.starter.core.common.AppResult
import com.example.starter.domain.repository.SampleRepository
import com.example.starter.domain.usecase.RefreshSampleItemsUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class RefreshSampleItemsUseCaseTest {
    @Test
    fun `invoke delegates to repository refresh`() =
        runTest {
            val repository = mockk<SampleRepository>()
            coEvery { repository.refresh() } returns AppResult.Success(Unit)

            val result = RefreshSampleItemsUseCase(repository)()

            assertEquals(AppResult.Success(Unit), result)
            coVerify(exactly = 1) { repository.refresh() }
        }
}
