package com.example.starter.core.datastore

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.starter.core.testing.android.FakeStringCipher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SecureStoreTest {
    @get:Rule
    val tmp = TemporaryFolder()

    private val cipher = FakeStringCipher()
    private val dispatcher = UnconfinedTestDispatcher()

    private fun dataStore(scope: CoroutineScope) =
        PreferenceDataStoreFactory.create(scope = scope) {
            File(tmp.root, "secure_prefs.preferences_pb")
        }

    @Test
    fun `put then get returns plain value`() =
        runTest {
            val store = DataStoreSecureStore(dataStore(backgroundScope), cipher, dispatcher)

            store.put(KEY, "secret")

            assertEquals("secret", store.get(KEY))
        }

    @Test
    fun `stored value is not plain text`() =
        runTest {
            val dataStore = dataStore(backgroundScope)
            val store = DataStoreSecureStore(dataStore, cipher, dispatcher)

            store.put(KEY, "secret")

            val raw = dataStore.data.first()[stringPreferencesKey(KEY)]
            assertTrue(raw.orEmpty().startsWith("enc:"))
            assertFalse(raw == "secret")
        }

    @Test
    fun `returns null and clears value when decrypt fails`() =
        runTest {
            val dataStore = dataStore(backgroundScope)
            val store = DataStoreSecureStore(dataStore, cipher, dispatcher)
            store.put(KEY, "secret")

            cipher.failDecrypt = true

            assertNull(store.get(KEY))
            assertFalse(dataStore.data.first().contains(stringPreferencesKey(KEY)))
        }

    @Test
    fun `other keys survive when decrypt fails`() =
        runTest {
            val dataStore = dataStore(backgroundScope)
            val store = DataStoreSecureStore(dataStore, cipher, dispatcher)
            store.put(KEY, "secret")
            store.put(OTHER_KEY, "other")

            cipher.failDecrypt = true
            assertNull(store.get(KEY))

            assertEquals("enc:other", dataStore.data.first()[stringPreferencesKey(OTHER_KEY)])
            cipher.failDecrypt = false
            assertEquals("other", store.get(OTHER_KEY))
        }

    @Test
    fun `remove deletes value`() =
        runTest {
            val store = DataStoreSecureStore(dataStore(backgroundScope), cipher, dispatcher)
            store.put(KEY, "secret")

            store.remove(KEY)

            assertNull(store.get(KEY))
        }

    private companion object {
        const val KEY = "token"
        const val OTHER_KEY = "refresh_token"
    }
}
