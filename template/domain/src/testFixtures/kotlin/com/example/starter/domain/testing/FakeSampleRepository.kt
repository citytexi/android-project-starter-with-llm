package com.example.starter.domain.testing

import com.example.starter.core.common.AppResult
import com.example.starter.domain.model.SampleItem
import com.example.starter.domain.repository.SampleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first

/**
 * In-memory [SampleRepository] for tests. Feed it to the real use cases instead of mocking them.
 *
 * - [items] and [lastRefreshedAt] are the observed values; set them to emit.
 * - [refresh] returns [refreshResult]. While [refreshGate] is `false` it suspends, so a test can
 *   check the "refreshing" state before letting it finish.
 */
class FakeSampleRepository(
    items: List<SampleItem> = emptyList(),
    lastRefreshedAt: Long? = null,
) : SampleRepository {
    val items = MutableStateFlow(items)
    val lastRefreshedAt = MutableStateFlow(lastRefreshedAt)
    val refreshGate = MutableStateFlow(true)
    var refreshResult: AppResult<Unit> = AppResult.Success(Unit)

    /** Number of [refresh] calls so far. */
    var refreshCount: Int = 0
        private set

    override fun observeItems(): Flow<List<SampleItem>> = items

    override fun observeLastRefreshedAt(): Flow<Long?> = lastRefreshedAt

    override suspend fun refresh(): AppResult<Unit> {
        refreshCount++
        refreshGate.first { it }
        return refreshResult
    }
}
