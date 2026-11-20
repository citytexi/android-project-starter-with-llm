package com.example.starter.core.datastore

import javax.inject.Qualifier

/** The encrypted `secure_prefs` DataStore behind [SecureStore]. Only [DataStoreSecureStore] uses it. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class SecurePreferences
