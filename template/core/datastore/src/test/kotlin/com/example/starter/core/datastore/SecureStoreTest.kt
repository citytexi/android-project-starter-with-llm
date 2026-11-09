package com.example.starter.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.starter.core.testing.android.FakeStringCipher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SecureStoreTest {
    @get:Rule
    val tmp = TemporaryFolder()

    private val cipher = FakeStringCipher()

    private fun TestScope.dataStore() =
        PreferenceDataStoreFactory.create(scope = backgroundScope) {
            File(tmp.root, "secure_prefs.preferences_pb")
        }

    // The dispatcher shares the test scheduler, so virtual time and runTest stay in sync.
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun TestScope.store(dataStore: DataStore<Preferences> = dataStore()) =
        DataStoreSecureStore(dataStore, cipher, UnconfinedTestDispatcher(testScheduler))

    @Test
    fun `put then get returns plain value`() =
        runTest {
            val store = store()

            store.put(KEY, "secret")

            assertEquals("secret", store.get(KEY))
        }

    @Test
    fun `stored value is not plain text`() =
        runTest {
            val dataStore = dataStore()
            val store = store(dataStore)

            store.put(KEY, "secret")

            val raw = dataStore.data.first()[stringPreferencesKey(KEY)]
            assertTrue(raw.orEmpty().startsWith("enc:"))
            assertFalse(raw == "secret")
        }

    @Test
    fun `returns null and clears value when decrypt fails`() =
        runTest {
            val dataStore = dataStore()
            val store = store(dataStore)
            store.put(KEY, "secret")

            cipher.failDecrypt = true

            assertNull(store.get(KEY))
            assertFalse(dataStore.data.first().contains(stringPreferencesKey(KEY)))
        }

    @Test
    fun `returns null but keeps value when keystore is temporarily unavailable`() =
        runTest {
            val dataStore = dataStore()
            val store = store(dataStore)
            store.put(KEY, "secret")

            cipher.keystoreUnavailable = true
            assertNull(store.get(KEY))

            assertEquals("enc:secret", dataStore.data.first()[stringPreferencesKey(KEY)])
            cipher.keystoreUnavailable = false
            assertEquals("secret", store.get(KEY))
        }

    @Test
    fun `other keys survive when decrypt fails`() =
        runTest {
            val dataStore = dataStore()
            val store = store(dataStore)
            store.put(KEY, "secret")
            store.put(OTHER_KEY, "other")

            cipher.failDecrypt = true
            assertNull(store.get(KEY))

            assertEquals("enc:other", dataStore.data.first()[stringPreferencesKey(OTHER_KEY)])
            cipher.failDecrypt = false
            assertEquals("other", store.get(OTHER_KEY))
        }

    @Test
    fun `get returns null when the file cannot be read`() =
        runTest {
            assertNull(store(FailingDataStore(IOException("disk"))).get(KEY))
        }

    @Test(expected = IllegalStateException::class)
    fun `get rethrows errors that are not IOException`() =
        runTest {
            store(FailingDataStore(IllegalStateException("bug"))).get(KEY)
        }

    @Test
    fun `remove deletes value`() =
        runTest {
            val store = store()
            store.put(KEY, "secret")

            store.remove(KEY)

            assertNull(store.get(KEY))
        }

    private class FailingDataStore(
        private val error: Throwable,
    ) : DataStore<Preferences> {
        override val data: Flow<Preferences> = flow { throw error }

        override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences = throw error
    }

    private companion object {
        const val KEY = "token"
        const val OTHER_KEY = "refresh_token"
    }
}
