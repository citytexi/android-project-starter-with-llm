package com.example.starter.core.datastore

/**
 * Encrypts and decrypts short strings for [SecureStore].
 *
 * The encoded form is Base64(IV || ciphertext). Implementations never log plaintext.
 */
interface StringCipher {
    fun encrypt(plain: String): String

    /**
     * Returns `null` when [encoded] cannot be decrypted: bad tag, missing, invalidated or
     * unrecoverable key, malformed Base64, or input shorter than IV plus tag.
     */
    fun decrypt(encoded: String): String?
}
