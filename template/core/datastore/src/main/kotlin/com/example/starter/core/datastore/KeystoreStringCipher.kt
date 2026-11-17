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
import javax.crypto.AEADBadTagException
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
 * Base64(IV || ciphertext+tag) with a 128-bit tag. [decrypt] never throws:
 *
 * - [DecryptResult.Unreadable] (the caller deletes the value): bad tag, malformed input, no key,
 *   or a permanently invalidated or unrecoverable key. In the last two cases the alias is also
 *   deleted so the next [encrypt] creates a new key.
 * - [DecryptResult.Unavailable] (the caller keeps the value): `KeyStoreException`,
 *   `ProviderException`, I/O and other key store errors that may pass on a later try.
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
            // One retry with a fresh key, outside the try so a second failure propagates.
            val cipher = tryInitEncrypt() ?: initEncrypt(getOrCreateKey())
            val iv = cipher.iv
            check(iv.size == IV_SIZE_BYTES) { "Unexpected GCM IV size" }
            val ciphertext = cipher.doFinal(plain.toByteArray(Charsets.UTF_8))
            return Base64.encodeToString(iv + ciphertext, Base64.NO_WRAP)
        }

        // Order matters: the specific GeneralSecurityException subclasses come before the general one.
        override fun decrypt(encoded: String): DecryptResult =
            try {
                decryptOrThrow(encoded)
            } catch (_: KeyPermanentlyInvalidatedException) {
                deleteKey()
                DecryptResult.Unreadable
            } catch (_: UnrecoverableKeyException) {
                deleteKey()
                DecryptResult.Unreadable
            } catch (_: AEADBadTagException) {
                // The value is corrupt or was written with an older key. The key itself is fine.
                DecryptResult.Unreadable
            } catch (_: IllegalArgumentException) {
                // Malformed Base64.
                DecryptResult.Unreadable
            } catch (_: GeneralSecurityException) {
                // KeyStoreException and the like: the key store may work on the next call.
                DecryptResult.Unavailable
            } catch (_: IOException) {
                DecryptResult.Unavailable
            } catch (_: ProviderException) {
                // Keystore daemon or binder failure.
                DecryptResult.Unavailable
            }

        /** Returns `null` after deleting a broken key (invalidated or unrecoverable entry). */
        private fun tryInitEncrypt(): Cipher? =
            try {
                initEncrypt(getOrCreateKey())
            } catch (_: KeyPermanentlyInvalidatedException) {
                deleteKey()
                null
            } catch (_: UnrecoverableKeyException) {
                deleteKey()
                null
            }

        private fun decryptOrThrow(encoded: String): DecryptResult {
            val bytes = Base64.decode(encoded, Base64.NO_WRAP)
            // Too short for IV plus tag, or no key (deleted, or app data restored without it).
            val key = existingKey()
            if (bytes.size < IV_SIZE_BYTES + TAG_SIZE_BITS / Byte.SIZE_BITS || key == null) return DecryptResult.Unreadable
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(TAG_SIZE_BITS, bytes, 0, IV_SIZE_BYTES))
            val plain = cipher.doFinal(bytes, IV_SIZE_BYTES, bytes.size - IV_SIZE_BYTES)
            return DecryptResult.Success(String(plain, Charsets.UTF_8))
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

        /**
         * Known race, accepted: [lock] guards create and delete, not whole encrypt/decrypt calls.
         * If one thread deletes the key here while another has just encrypted with a fresh key,
         * that new value becomes unreadable. Deletion only follows
         * [KeyPermanentlyInvalidatedException] or [UnrecoverableKeyException]. The key is not
         * auth-bound (no `setUserAuthenticationRequired`), so invalidation by lock-screen or
         * biometric changes does not apply and the race is practically unreachable.
         */
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
