package com.example.starter

import com.example.starter.core.datastore.SettingsDataSource
import com.example.starter.core.testing.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.io.IOException
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MainViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `isReady becomes true after first settings emission`() =
        runTest {
            val lastRefreshedAt = MutableSharedFlow<Long?>()
            val settings = mockk<SettingsDataSource> { every { this@mockk.lastRefreshedAt } returns lastRefreshedAt }

            val viewModel = MainViewModel(settings)
            assertFalse(viewModel.isReady.value)

            lastRefreshedAt.emit(null)
            assertTrue(viewModel.isReady.value)
        }

    @Test
    fun `isReady becomes true when settings fail to load`() =
        runTest {
            val failing = flow<Long?> { throw IOException("disk") }
            val settings = mockk<SettingsDataSource> { every { lastRefreshedAt } returns failing }

            assertTrue(MainViewModel(settings).isReady.value)
        }
}
