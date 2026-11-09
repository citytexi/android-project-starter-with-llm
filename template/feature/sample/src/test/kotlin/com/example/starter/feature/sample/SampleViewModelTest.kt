package com.example.starter.feature.sample

import com.example.starter.core.common.AppResult
import com.example.starter.core.testing.MainDispatcherRule
import com.example.starter.domain.model.SampleItem
import com.example.starter.domain.testing.FakeSampleRepository
import com.example.starter.domain.usecase.ObserveLastRefreshedAtUseCase
import com.example.starter.domain.usecase.ObserveSampleItemsUseCase
import com.example.starter.domain.usecase.RefreshSampleItemsUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SampleViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeSampleRepository()

    // Real use cases on a fake repository: the test covers the same wiring as the app.
    private fun viewModel() =
        SampleViewModel(
            ObserveSampleItemsUseCase(repository),
            ObserveLastRefreshedAtUseCase(repository),
            RefreshSampleItemsUseCase(repository),
        )

    // uiState is WhileSubscribed, so keep a collector running for the whole test.
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun TestScope.collect(viewModel: SampleViewModel) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
    }

    @Test
    fun `items from use case appear in state`() =
        runTest {
            val items = listOf(SampleItem(1, "a"), SampleItem(2, "b"))
            repository.items.value = items
            repository.lastRefreshedAt.value = 7L
            val viewModel = viewModel()
            collect(viewModel)

            assertEquals(items, viewModel.uiState.value.items)
            assertEquals(7L, viewModel.uiState.value.lastRefreshedAt)
        }

    @Test
    fun `IOException maps to Network error and stops refreshing`() =
        runTest {
            repository.refreshResult = AppResult.Failure(IOException("offline"))
            val viewModel = viewModel()
            collect(viewModel)

            viewModel.onAction(SampleAction.Refresh)

            assertEquals(SampleError.Network, viewModel.uiState.value.error)
            assertFalse(viewModel.uiState.value.isRefreshing)
        }

    @Test
    fun `other failures map to Unknown error`() =
        runTest {
            repository.refreshResult = AppResult.Failure(IllegalStateException("bug"))
            val viewModel = viewModel()
            collect(viewModel)

            viewModel.onAction(SampleAction.Refresh)

            assertEquals(SampleError.Unknown, viewModel.uiState.value.error)
        }

    @Test
    fun `refresh shows refreshing until the use case returns`() =
        runTest {
            repository.refreshGate.value = false
            val viewModel = viewModel()
            collect(viewModel)

            viewModel.onAction(SampleAction.Refresh)
            assertTrue(viewModel.uiState.value.isRefreshing)

            repository.refreshGate.value = true
            assertFalse(viewModel.uiState.value.isRefreshing)
            assertNull(viewModel.uiState.value.error)
        }

    @Test
    fun `second Refresh while refreshing is ignored`() =
        runTest {
            repository.refreshGate.value = false
            val viewModel = viewModel()
            collect(viewModel)

            viewModel.onAction(SampleAction.Refresh)
            viewModel.onAction(SampleAction.Refresh)
            repository.refreshGate.value = true

            assertEquals(1, repository.refreshCount)
        }

    @Test
    fun `DismissError clears error`() =
        runTest {
            repository.refreshResult = AppResult.Failure(IOException("offline"))
            val viewModel = viewModel()
            collect(viewModel)
            viewModel.onAction(SampleAction.Refresh)

            viewModel.onAction(SampleAction.DismissError)

            assertNull(viewModel.uiState.value.error)
        }
}
