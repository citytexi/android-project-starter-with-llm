package com.example.starter.domain

import com.example.starter.domain.model.SampleItem
import com.example.starter.domain.testing.FakeSampleRepository
import com.example.starter.domain.usecase.ObserveSampleItemsUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveSampleItemsUseCaseTest {
    @Test
    fun `invoke emits repository items`() =
        runTest {
            val items = listOf(SampleItem(1, "a"))
            val repository = FakeSampleRepository(items = items)

            assertEquals(items, ObserveSampleItemsUseCase(repository)().first())
        }
}
