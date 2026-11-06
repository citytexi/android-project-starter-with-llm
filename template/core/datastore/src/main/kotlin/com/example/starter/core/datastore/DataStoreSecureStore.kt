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
import javax.inject.Inject
import javax.inject.Named

/**
 * [SecureStore] on the `secure_prefs` Preferences DataStore. Values are stored as [StringCipher] output.
 *
 * A value that fails to decrypt (key lost after reinstall or backup restore, corrupt data) is
 * removed and reported as `null`, so callers never crash on it. Cipher calls (Keystore binder IPC)
 * run on [ioDispatcher].
 */
class DataStoreSecureStore
    @Inject
    constructor(
        @param:Named("secure") private val dataStore: DataStore<Preferences>,
        private val cipher: StringCipher,
        @param:Dispatcher(StarterDispatcher.IO) private val ioDispatcher: CoroutineDispatcher,
    ) : SecureStore {
        override suspend fun get(key: String): String? {
            val prefKey = stringPreferencesKey(key)
            val encoded = dataStore.data.first()[prefKey] ?: return null
            val plain = withContext(ioDispatcher) { cipher.decrypt(encoded) }
            if (plain == null) {
                // Remove only the value that failed; a value written in between survives.
                dataStore.edit { if (it[prefKey] == encoded) it.remove(prefKey) }
            }
            return plain
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
    }
