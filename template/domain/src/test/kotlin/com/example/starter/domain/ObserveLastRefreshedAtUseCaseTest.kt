package com.example.starter.domain

import com.example.starter.domain.testing.FakeSampleRepository
import com.example.starter.domain.usecase.ObserveLastRefreshedAtUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveLastRefreshedAtUseCaseTest {
    @Test
    fun `invoke emits repository last refreshed time`() =
        runTest {
            val repository = FakeSampleRepository(lastRefreshedAt = 42L)

            assertEquals(42L, ObserveLastRefreshedAtUseCase(repository)().first())
        }
}
