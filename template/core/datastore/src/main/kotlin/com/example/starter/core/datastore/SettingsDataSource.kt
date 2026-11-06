package com.example.starter.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Named

/** Plain (unencrypted) app settings on the `settings` Preferences DataStore. */
class SettingsDataSource
    @Inject
    constructor(
        @param:Named("settings") private val dataStore: DataStore<Preferences>,
    ) {
        val lastRefreshedAt: Flow<Long?> = dataStore.data.map { it[LAST_REFRESHED_AT] }

        suspend fun setLastRefreshedAt(epochMillis: Long) {
            dataStore.edit { it[LAST_REFRESHED_AT] = epochMillis }
        }

        private companion object {
            val LAST_REFRESHED_AT = longPreferencesKey("last_refreshed_at")
        }
    }
