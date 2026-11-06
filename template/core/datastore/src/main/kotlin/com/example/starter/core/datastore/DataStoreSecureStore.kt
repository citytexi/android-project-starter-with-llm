package com.example.starter.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Named

/**
 * [SecureStore] on the `secure_prefs` Preferences DataStore. Values are stored as [StringCipher] output.
 *
 * A value that fails to decrypt (key lost after reinstall or backup restore, corrupt data) is
 * removed and reported as `null`, so callers never crash on it.
 */
class DataStoreSecureStore
    @Inject
    constructor(
        @param:Named("secure") private val dataStore: DataStore<Preferences>,
        private val cipher: StringCipher,
    ) : SecureStore {
        override suspend fun get(key: String): String? {
            val encoded = dataStore.data.first()[stringPreferencesKey(key)] ?: return null
            val plain = cipher.decrypt(encoded)
            if (plain == null) remove(key)
            return plain
        }

        override suspend fun put(
            key: String,
            value: String,
        ) {
            val encoded = cipher.encrypt(value)
            dataStore.edit { it[stringPreferencesKey(key)] = encoded }
        }

        override suspend fun remove(key: String) {
            dataStore.edit { it.remove(stringPreferencesKey(key)) }
        }
    }
