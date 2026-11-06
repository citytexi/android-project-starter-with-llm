package com.example.starter.core.datastore

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import android.util.Base64
import java.io.IOException
import java.security.GeneralSecurityException
import java.security.KeyStore
import java.security.ProviderException
import java.security.UnrecoverableKeyException
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * [StringCipher] backed by an AES-256/GCM/NoPadding key in AndroidKeyStore (alias [KEY_ALIAS]).
 *
 * Every [encrypt] call uses a fresh random IV chosen by the cipher. The output is
 * Base64(IV || ciphertext+tag) with a 128-bit tag. [decrypt] returns `null` on any failure.
 * When the key is permanently invalidated or unrecoverable, the alias is deleted so the next
 * [encrypt] creates a new key.
 *
 * Robolectric has no AndroidKeyStore, so this class is not unit tested. JVM tests use
 * `FakeStringCipher` from `core:testing:android`.
 */
@Singleton
class KeystoreStringCipher
    @Inject
    constructor() : StringCipher {
        private val lock = Any()

        override fun encrypt(plain: String): String {
            val cipher =
                try {
                    initEncrypt(getOrCreateKey())
                } catch (_: KeyPermanentlyInvalidatedException) {
                    deleteKey()
                    initEncrypt(getOrCreateKey())
                }
            val iv = cipher.iv
            check(iv.size == IV_SIZE_BYTES) { "Unexpected GCM IV size" }
            val ciphertext = cipher.doFinal(plain.toByteArray(Charsets.UTF_8))
            return Base64.encodeToString(iv + ciphertext, Base64.NO_WRAP)
        }

        override fun decrypt(encoded: String): String? =
            try {
                decryptOrThrow(encoded)
            } catch (_: KeyPermanentlyInvalidatedException) {
                deleteKey()
                null
            } catch (_: UnrecoverableKeyException) {
                deleteKey()
                null
            } catch (_: GeneralSecurityException) {
                // Includes AEADBadTagException: the value is corrupt, the key is still fine.
                null
            } catch (_: IllegalArgumentException) {
                // Malformed Base64.
                null
            } catch (_: IOException) {
                null
            } catch (_: ProviderException) {
                null
            }

        private fun decryptOrThrow(encoded: String): String? {
            val bytes = Base64.decode(encoded, Base64.NO_WRAP)
            val key = existingKey()
            if (bytes.size < IV_SIZE_BYTES + TAG_SIZE_BITS / Byte.SIZE_BITS || key == null) return null
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(TAG_SIZE_BITS, bytes, 0, IV_SIZE_BYTES))
            return String(cipher.doFinal(bytes, IV_SIZE_BYTES, bytes.size - IV_SIZE_BYTES), Charsets.UTF_8)
        }

        private fun initEncrypt(key: SecretKey): Cipher {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, key)
            return cipher
        }

        private fun keyStore(): KeyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }

        private fun existingKey(): SecretKey? = keyStore().getKey(KEY_ALIAS, null) as? SecretKey

        private fun getOrCreateKey(): SecretKey =
            synchronized(lock) {
                existingKey() ?: generateKey()
            }

        private fun generateKey(): SecretKey {
            val spec =
                KeyGenParameterSpec
                    .Builder(KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(KEY_SIZE_BITS)
                    .setRandomizedEncryptionRequired(true)
                    .build()
            return KeyGenerator
                .getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
                .apply { init(spec) }
                .generateKey()
        }

        private fun deleteKey() {
            synchronized(lock) {
                try {
                    keyStore().deleteEntry(KEY_ALIAS)
                } catch (_: GeneralSecurityException) {
                    // Nothing to delete or keystore unavailable; the next encrypt retries.
                } catch (_: IOException) {
                    // Same as above.
                }
            }
        }

        private companion object {
            const val ANDROID_KEYSTORE = "AndroidKeyStore"
            const val KEY_ALIAS = "starter_secure_key"
            const val TRANSFORMATION = "AES/GCM/NoPadding"
            const val KEY_SIZE_BITS = 256
            const val TAG_SIZE_BITS = 128
            const val IV_SIZE_BYTES = 12
        }
    }
