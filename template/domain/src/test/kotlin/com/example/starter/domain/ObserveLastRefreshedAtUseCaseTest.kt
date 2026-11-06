package com.example.starter.domain

import com.example.starter.domain.repository.SampleRepository
import com.example.starter.domain.usecase.ObserveLastRefreshedAtUseCase
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveLastRefreshedAtUseCaseTest {
    @Test
    fun `invoke emits repository last refreshed time`() =
        runTest {
            val repository = mockk<SampleRepository>()
            every { repository.observeLastRefreshedAt() } returns flowOf(42L)

            assertEquals(42L, ObserveLastRefreshedAtUseCase(repository)().first())
        }
}
