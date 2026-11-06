package com.example.starter.feature.sample

import com.example.starter.core.common.AppResult
import com.example.starter.core.testing.MainDispatcherRule
import com.example.starter.domain.model.SampleItem
import com.example.starter.domain.usecase.ObserveLastRefreshedAtUseCase
import com.example.starter.domain.usecase.ObserveSampleItemsUseCase
import com.example.starter.domain.usecase.RefreshSampleItemsUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

class SampleViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val observeItems = mockk<ObserveSampleItemsUseCase>()
    private val observeLastRefreshedAt = mockk<ObserveLastRefreshedAtUseCase>()
    private val refreshItems = mockk<RefreshSampleItemsUseCase>()

    private fun viewModel(
        items: List<SampleItem> = emptyList(),
        lastRefreshedAt: Long? = null,
    ): SampleViewModel {
        every { observeItems() } returns flowOf(items)
        every { observeLastRefreshedAt() } returns flowOf(lastRefreshedAt)
        return SampleViewModel(observeItems, observeLastRefreshedAt, refreshItems)
    }

    // uiState is WhileSubscribed, so keep a collector running for the whole test.
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun TestScope.collect(viewModel: SampleViewModel) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
    }

    @Test
    fun `items from use case appear in state`() =
        runTest {
            val items = listOf(SampleItem(1, "a"), SampleItem(2, "b"))
            val viewModel = viewModel(items = items, lastRefreshedAt = 7L)
            collect(viewModel)

            assertEquals(items, viewModel.uiState.value.items)
            assertEquals(7L, viewModel.uiState.value.lastRefreshedAt)
        }

    @Test
    fun `refresh failure sets errorMessage and stops refreshing`() =
        runTest {
            coEvery { refreshItems() } returns AppResult.Failure(IllegalStateException("offline"))
            val viewModel = viewModel()
            collect(viewModel)

            viewModel.onAction(SampleAction.Refresh)

            assertEquals("offline", viewModel.uiState.value.errorMessage)
            assertFalse(viewModel.uiState.value.isRefreshing)
        }

    @Test
    fun `refresh shows refreshing until the use case returns`() =
        runTest {
            val gate = MutableStateFlow(false)
            coEvery { refreshItems() } coAnswers {
                gate.first { it }
                AppResult.Success(Unit)
            }
            val viewModel = viewModel()
            collect(viewModel)

            viewModel.onAction(SampleAction.Refresh)
            assertEquals(true, viewModel.uiState.value.isRefreshing)

            gate.value = true
            assertFalse(viewModel.uiState.value.isRefreshing)
            assertNull(viewModel.uiState.value.errorMessage)
        }

    @Test
    fun `DismissError clears error`() =
        runTest {
            coEvery { refreshItems() } returns AppResult.Failure(IllegalStateException("offline"))
            val viewModel = viewModel()
            collect(viewModel)
            viewModel.onAction(SampleAction.Refresh)

            viewModel.onAction(SampleAction.DismissError)

            assertNull(viewModel.uiState.value.errorMessage)
        }
}
