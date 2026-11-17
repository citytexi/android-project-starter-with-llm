package com.example.starter.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.starter.core.common.Dispatcher
import com.example.starter.core.common.StarterDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

/**
 * [SecureStore] on the `secure_prefs` Preferences DataStore. Values are stored as [StringCipher] output.
 *
 * A value that can never be decrypted ([DecryptResult.Unreadable]: key lost after reinstall or
 * backup restore, corrupt data) is removed and reported as `null`. A temporary key store failure
 * ([DecryptResult.Unavailable]) or an unreadable file also reads as `null` but keeps the value.
 * Cipher calls (Keystore binder IPC) run on [ioDispatcher].
 */
class DataStoreSecureStore
    @Inject
    constructor(
        @param:SecurePreferences private val dataStore: DataStore<Preferences>,
        private val cipher: StringCipher,
        @param:Dispatcher(StarterDispatcher.IO) private val ioDispatcher: CoroutineDispatcher,
    ) : SecureStore {
        override suspend fun get(key: String): String? {
            val prefKey = stringPreferencesKey(key)
            // Same policy as SettingsDataSource: an unreadable file reads as "no value".
            val encoded =
                try {
                    dataStore.data.first()[prefKey]
                } catch (_: IOException) {
                    null
                } ?: return null
            return when (val result = withContext(ioDispatcher) { cipher.decrypt(encoded) }) {
                is DecryptResult.Success -> result.plain
                DecryptResult.Unavailable -> null
                DecryptResult.Unreadable -> {
                    removeIfUnchanged(prefKey, encoded)
                    null
                }
            }
        }

        override suspend fun put(
            key: String,
            value: String,
        ) {
            val encoded = withContext(ioDispatcher) { cipher.encrypt(value) }
            dataStore.edit { it[stringPreferencesKey(key)] = encoded }
        }

        override suspend fun remove(key: String) {
            dataStore.edit { it.remove(stringPreferencesKey(key)) }
        }

        /** Removes only the value that failed; a value written in between survives. */
        private suspend fun removeIfUnchanged(
            prefKey: Preferences.Key<String>,
            encoded: String,
        ) {
            try {
                dataStore.edit { if (it[prefKey] == encoded) it.remove(prefKey) }
            } catch (_: IOException) {
                // The value stays; the next get() fails to decrypt it again and retries the removal.
            }
        }
    }
