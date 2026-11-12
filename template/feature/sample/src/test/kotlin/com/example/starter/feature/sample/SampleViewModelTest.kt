package com.example.starter.feature.sample

import app.cash.turbine.test
import com.example.starter.core.common.HttpStatusException
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

    // Data is observed only while state has a collector (launchWhileSubscribed), like a visible screen.
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun TestScope.collect(viewModel: SampleViewModel) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.container.state.collect {} }
    }

    private suspend fun SampleViewModel.refreshAndAwaitError(): SampleError {
        var error: SampleError? = null
        container.sideEffect.test {
            onIntent(SampleIntent.Refresh)
            error = (awaitItem() as SampleSideEffect.ShowError).error
        }
        return checkNotNull(error)
    }

    @Test
    fun `items from use case appear in state`() =
        runTest {
            val items = listOf(SampleItem(1, "a"), SampleItem(2, "b"))
            repository.items.value = items
            repository.lastRefreshedAt.value = 7L
            val viewModel = viewModel()
            collect(viewModel)

            assertEquals(items, viewModel.container.state.value.items)
            assertEquals(7L, viewModel.container.state.value.lastRefreshedAt)
        }

    @Test
    fun `items are not observed without a collector`() =
        runTest {
            repository.items.value = listOf(SampleItem(1, "a"))
            val viewModel = viewModel()

            assertEquals(emptyList(), viewModel.container.state.value.items)
        }

    @Test
    fun `IOException posts Network error and stops refreshing`() =
        runTest {
            repository.refreshError = IOException("offline")
            val viewModel = viewModel()
            collect(viewModel)

            assertEquals(SampleError.Network, viewModel.refreshAndAwaitError())
            assertFalse(viewModel.container.state.value.isRefreshing)
        }

    @Test
    fun `HttpStatusException posts Server error, not Network`() =
        runTest {
            repository.refreshError = HttpStatusException(503, RuntimeException("boom"))
            val viewModel = viewModel()
            collect(viewModel)

            assertEquals(SampleError.Server, viewModel.refreshAndAwaitError())
        }

    @Test
    fun `other failures post Unknown error`() =
        runTest {
            repository.refreshError = IllegalStateException("bug")
            val viewModel = viewModel()
            collect(viewModel)

            assertEquals(SampleError.Unknown, viewModel.refreshAndAwaitError())
        }

    @Test
    fun `successful refresh posts no side effect`() =
        runTest {
            val viewModel = viewModel()
            collect(viewModel)

            viewModel.container.sideEffect.test {
                viewModel.onIntent(SampleIntent.Refresh)
                expectNoEvents()
            }
        }

    @Test
    fun `refresh shows refreshing until the use case returns`() =
        runTest {
            repository.refreshGate.value = false
            val viewModel = viewModel()
            collect(viewModel)

            viewModel.onIntent(SampleIntent.Refresh)
            assertTrue(viewModel.container.state.value.isRefreshing)

            repository.refreshGate.value = true
            assertFalse(viewModel.container.state.value.isRefreshing)
        }

    @Test
    fun `second Refresh while refreshing is ignored`() =
        runTest {
            repository.refreshGate.value = false
            val viewModel = viewModel()
            collect(viewModel)

            viewModel.onIntent(SampleIntent.Refresh)
            viewModel.onIntent(SampleIntent.Refresh)
            repository.refreshGate.value = true

            assertEquals(1, repository.refreshCount)
        }
}
