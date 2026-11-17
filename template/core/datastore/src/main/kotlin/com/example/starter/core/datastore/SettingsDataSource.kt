package com.example.starter.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

/** Plain (unencrypted) app settings on the `settings` Preferences DataStore. */
class SettingsDataSource
    @Inject
    constructor(
        @param:SettingsPreferences private val dataStore: DataStore<Preferences>,
    ) {
        // An unreadable file reads as empty settings instead of crashing every collector.
        val lastRefreshedAt: Flow<Long?> =
            dataStore.data
                .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
                .map { it[LAST_REFRESHED_AT] }

        suspend fun setLastRefreshedAt(epochMillis: Long) {
            dataStore.edit { it[LAST_REFRESHED_AT] = epochMillis }
        }

        private companion object {
            val LAST_REFRESHED_AT = longPreferencesKey("last_refreshed_at")
        }
    }
