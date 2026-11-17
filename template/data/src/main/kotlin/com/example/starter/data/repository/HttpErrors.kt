package com.example.starter.data.repository

import com.example.starter.core.common.HttpStatusException
import retrofit2.HttpException

/**
 * Runs a Retrofit call and rethrows [HttpException] as [HttpStatusException] (an `IOException`).
 * Connection errors are already `IOException`, so callers only check one type for "network".
 */
internal inline fun <T> mapHttpErrors(block: () -> T): T =
    try {
        block()
    } catch (e: HttpException) {
        throw HttpStatusException(e.code(), e)
    }
