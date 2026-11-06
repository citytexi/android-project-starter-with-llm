package com.example.starter.core.common

import kotlin.coroutines.cancellation.CancellationException

/** Outcome of an operation: either [Success] with data or [Failure] with the cause. */
sealed interface AppResult<out T> {
    data class Success<T>(
        val data: T,
    ) : AppResult<T>

    data class Failure(
        val error: Throwable,
    ) : AppResult<Nothing>
}

/** Runs [block] and wraps the outcome. [CancellationException] is rethrown, never wrapped. */
@Suppress("TooGenericExceptionCaught")
inline fun <T> appRunCatching(block: () -> T): AppResult<T> =
    try {
        AppResult.Success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        AppResult.Failure(e)
    }
