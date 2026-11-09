package com.example.starter.data.repository

import androidx.room.withTransaction
import com.example.starter.core.common.AppResult
import com.example.starter.core.common.Dispatcher
import com.example.starter.core.common.StarterDispatcher
import com.example.starter.core.common.appRunCatching
import com.example.starter.core.database.StarterDatabase
import com.example.starter.core.database.dao.SampleItemDao
import com.example.starter.core.database.model.SampleItemEntity
import com.example.starter.core.datastore.SettingsDataSource
import com.example.starter.core.network.SampleApi
import com.example.starter.domain.model.SampleItem
import com.example.starter.domain.repository.SampleRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

/** Offline-first repository: the database is the source of truth, [refresh] pulls from the network. */
class SampleRepositoryImpl(
    private val database: StarterDatabase,
    private val api: SampleApi,
    private val dao: SampleItemDao,
    private val settings: SettingsDataSource,
    private val ioDispatcher: CoroutineDispatcher,
    private val clock: () -> Long,
) : SampleRepository {
    // Hilt cannot inject the function-typed `clock`, so Hilt uses this constructor.
    @Inject
    constructor(
        database: StarterDatabase,
        api: SampleApi,
        dao: SampleItemDao,
        settings: SettingsDataSource,
        @Dispatcher(StarterDispatcher.IO) ioDispatcher: CoroutineDispatcher,
    ) : this(database, api, dao, settings, ioDispatcher, System::currentTimeMillis)

    override fun observeItems(): Flow<List<SampleItem>> = dao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeLastRefreshedAt(): Flow<Long?> = settings.lastRefreshedAt

    // Retrofit suspend calls, Room and DataStore are already main-safe. withContext(ioDispatcher)
    // is kept on purpose: the DTO-to-entity mapping and any blocking work added later stay off the
    // main thread, and tests swap the dispatcher for a test one.
    override suspend fun refresh(): AppResult<Unit> =
        withContext(ioDispatcher) {
            appRunCatching {
                val entities = mapHttpErrors { api.getItems() }.map { SampleItemEntity(it.id, it.title) }
                // One transaction: a failed write rolls back, so the cache is never left empty.
                database.withTransaction {
                    dao.clear()
                    dao.upsertAll(entities)
                }
                // Written only after the transaction commits, so a failed write never records a
                // refresh time. If this write itself fails, the items are already saved and the
                // result is Failure; the next refresh records the time.
                settings.setLastRefreshedAt(clock())
            }
        }
}

private fun SampleItemEntity.toDomain() = SampleItem(id = id, title = title)
