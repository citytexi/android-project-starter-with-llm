package com.example.starter.core.datastore.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.example.starter.core.datastore.DataStoreSecureStore
import com.example.starter.core.datastore.KeystoreStringCipher
import com.example.starter.core.datastore.SecurePreferences
import com.example.starter.core.datastore.SecureStore
import com.example.starter.core.datastore.SettingsPreferences
import com.example.starter.core.datastore.StringCipher
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Binds the Keystore cipher and secure store, and provides both Preferences DataStores.
 *
 * Both DataStores replace a corrupt file with empty preferences instead of failing every read.
 * The IO `CoroutineDispatcher` for [DataStoreSecureStore] comes from `DispatcherModule` in `core:common`.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class DataStoreModule {
    @Binds
    abstract fun bindStringCipher(impl: KeystoreStringCipher): StringCipher

    @Binds
    abstract fun bindSecureStore(impl: DataStoreSecureStore): SecureStore

    companion object {
        /** File name is fixed: backup rules exclude `datastore/secure_prefs.preferences_pb`. */
        const val SECURE_PREFS_NAME = "secure_prefs"
        const val SETTINGS_NAME = "settings"

        @Provides
        @Singleton
        @SecurePreferences
        fun provideSecureDataStore(
            @ApplicationContext context: Context,
        ): DataStore<Preferences> =
            PreferenceDataStoreFactory.create(
                corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },
            ) {
                context.preferencesDataStoreFile(SECURE_PREFS_NAME)
            }

        @Provides
        @Singleton
        @SettingsPreferences
        fun provideSettingsDataStore(
            @ApplicationContext context: Context,
        ): DataStore<Preferences> =
            PreferenceDataStoreFactory.create(
                corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },
            ) {
                context.preferencesDataStoreFile(SETTINGS_NAME)
            }
    }
}
