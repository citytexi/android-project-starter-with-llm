package com.example.starter.core.common

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.coroutines.cancellation.CancellationException

/**
 * [runCatching] for blocks that call suspend functions. Every `suspend operator fun invoke` of a
 * UseCase returns its result through this (Konsist checks the `Result<T>` return type).
 *
 * The stdlib [runCatching] also catches [CancellationException]: a coroutine cancelled inside the
 * block (the screen was left) would look like a failed operation, and the parent would think the
 * child finished normally. This rethrows cancellation and wraps everything else.
 *
 * Without suspend calls in the block, use the stdlib [runCatching].
 *
 * Calls [block] in a `try` instead of passing it to `runCatching`, so the `callsInPlace` contract
 * holds (block runs exactly once in place).
 */
@OptIn(ExperimentalContracts::class)
@Suppress("TooGenericExceptionCaught") // Wrapping every failure into Result is the point.
inline fun <T> runSuspendCatching(block: () -> T): Result<T> {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }

    return try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        Result.failure(e)
    }
}
