package com.example.starter.core.datastore

/**
 * Encrypts and decrypts short strings for [SecureStore].
 *
 * The encoded form is Base64(IV || ciphertext). Implementations never log plaintext.
 */
interface StringCipher {
    /** Throws `GeneralSecurityException` or `ProviderException` when the key store cannot encrypt. */
    fun encrypt(plain: String): String

    /** Never throws. See [DecryptResult] for what each failure means for the stored value. */
    fun decrypt(encoded: String): DecryptResult
}

/** Outcome of [StringCipher.decrypt]. */
sealed interface DecryptResult {
    data class Success(
        val plain: String,
    ) : DecryptResult

    /**
     * The value can never be decrypted: bad tag (corrupt, or written with an older key), missing,
     * invalidated or unrecoverable key, malformed Base64, or input shorter than IV plus tag.
     * The caller deletes the value.
     */
    data object Unreadable : DecryptResult

    /**
     * The key store failed for a reason that may pass (`KeyStoreException`, `ProviderException`,
     * binder or I/O error). The value may still be fine, so the caller keeps it.
     */
    data object Unavailable : DecryptResult
}
