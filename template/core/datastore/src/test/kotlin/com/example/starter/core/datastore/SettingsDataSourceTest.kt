package com.example.starter.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import app.cash.turbine.test
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SettingsDataSourceTest {
    @get:Rule
    val tmp = TemporaryFolder()

    @Test
    fun `lastRefreshedAt emits null then saved value`() =
        runTest {
            val dataStore =
                PreferenceDataStoreFactory.create(scope = backgroundScope) {
                    File(tmp.root, "settings.preferences_pb")
                }
            val source = SettingsDataSource(dataStore)

            source.lastRefreshedAt.test {
                assertNull(awaitItem())

                source.setLastRefreshedAt(1_700_000_000_000L)

                assertEquals(1_700_000_000_000L, awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `lastRefreshedAt emits null when the file cannot be read`() =
        runTest {
            val source = SettingsDataSource(FailingDataStore(IOException("disk")))

            assertNull(source.lastRefreshedAt.first())
        }

    @Test(expected = IllegalStateException::class)
    fun `lastRefreshedAt rethrows errors that are not IOException`() =
        runTest {
            SettingsDataSource(FailingDataStore(IllegalStateException("bug"))).lastRefreshedAt.first()
        }

    private class FailingDataStore(
        private val error: Throwable,
    ) : DataStore<Preferences> {
        override val data: Flow<Preferences> = flow { throw error }

        override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences = throw error
    }
}
