package com.example.starter.core.testing.android

import com.example.starter.core.datastore.StringCipher

/**
 * Test [StringCipher]: adds and strips the `enc:` prefix. Robolectric has no AndroidKeyStore.
 *
 * Set [failDecrypt] to simulate a lost key or corrupt value.
 */
class FakeStringCipher(
    var failDecrypt: Boolean = false,
) : StringCipher {
    override fun encrypt(plain: String): String = PREFIX + plain

    override fun decrypt(encoded: String): String? {
        if (failDecrypt || !encoded.startsWith(PREFIX)) return null
        return encoded.removePrefix(PREFIX)
    }

    private companion object {
        const val PREFIX = "enc:"
    }
}
