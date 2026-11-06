package com.example.starter.core.datastore

/** Key-value store whose values are encrypted at rest. */
interface SecureStore {
    /** Returns the value, or `null` when it is absent or can no longer be decrypted. */
    suspend fun get(key: String): String?

    suspend fun put(
        key: String,
        value: String,
    )

    suspend fun remove(key: String)
}
