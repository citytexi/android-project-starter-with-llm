package com.example.starter.core.common

import java.io.IOException

/**
 * The server answered with a non-2xx HTTP status [code].
 *
 * `data` converts Retrofit's `HttpException` into this type, so layers above `data` see every
 * network failure as an [IOException] and never depend on Retrofit. Check [code] for cases such as
 * 401 (sign in again).
 */
class HttpStatusException(
    val code: Int,
    cause: Throwable? = null,
) : IOException("HTTP $code", cause)
