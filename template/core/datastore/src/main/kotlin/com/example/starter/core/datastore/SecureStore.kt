package com.example.starter.core.datastore

/** Key-value store whose values are encrypted at rest. */
interface SecureStore {
    /**
     * Returns the value, or `null` when it is absent or cannot be read now. Never throws for
     * storage or key store errors.
     *
     * - The value can never be decrypted (key lost after reinstall or backup restore, corrupt
     *   data): it is deleted, and later calls also return `null`.
     * - The key store is temporarily unavailable, or the file cannot be read: the value is kept,
     *   so a later call may return it.
     */
    suspend fun get(key: String): String?

    /**
     * Encrypts and saves [value]. Throws `IOException` when the file cannot be written, and
     * `GeneralSecurityException` or `ProviderException` when encryption fails. Callers that must
     * not crash catch these and report the failure.
     */
    suspend fun put(
        key: String,
        value: String,
    )

    /** Throws `IOException` when the file cannot be written. */
    suspend fun remove(key: String)
}
