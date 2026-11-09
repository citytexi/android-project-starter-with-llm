package com.example.starter.core.testing.android

import com.example.starter.core.datastore.DecryptResult
import com.example.starter.core.datastore.StringCipher

/**
 * Test [StringCipher]: adds and strips the `enc:` prefix. Robolectric has no AndroidKeyStore.
 *
 * Set [failDecrypt] to simulate a lost key or corrupt value ([DecryptResult.Unreadable]), or
 * [keystoreUnavailable] to simulate a temporary key store error ([DecryptResult.Unavailable]).
 */
class FakeStringCipher(
    var failDecrypt: Boolean = false,
    var keystoreUnavailable: Boolean = false,
) : StringCipher {
    override fun encrypt(plain: String): String = PREFIX + plain

    override fun decrypt(encoded: String): DecryptResult =
        when {
            keystoreUnavailable -> DecryptResult.Unavailable
            failDecrypt || !encoded.startsWith(PREFIX) -> DecryptResult.Unreadable
            else -> DecryptResult.Success(encoded.removePrefix(PREFIX))
        }

    private companion object {
        const val PREFIX = "enc:"
    }
}
