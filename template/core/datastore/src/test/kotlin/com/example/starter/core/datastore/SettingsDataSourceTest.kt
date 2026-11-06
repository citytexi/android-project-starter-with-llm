package com.example.starter.core.datastore

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
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
}
