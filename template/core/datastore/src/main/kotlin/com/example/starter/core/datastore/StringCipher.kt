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
