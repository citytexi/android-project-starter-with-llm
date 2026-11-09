package com.example.starter.data

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.starter.core.common.AppResult
import com.example.starter.core.common.HttpStatusException
import com.example.starter.core.database.StarterDatabase
import com.example.starter.core.database.dao.SampleItemDao
import com.example.starter.core.database.model.SampleItemEntity
import com.example.starter.core.datastore.SettingsDataSource
import com.example.starter.core.network.SampleApi
import com.example.starter.core.network.model.SampleItemResponse
import com.example.starter.data.repository.SampleRepositoryImpl
import com.example.starter.domain.model.SampleItem
import kotlinx.coroutines.flow.first
import okhttp3.ResponseBody.Companion.toResponseBody
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import retrofit2.HttpException
import retrofit2.Response
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SampleRepositoryImplTest {
    @get:Rule
    val tmp = TemporaryFolder()

    private lateinit var db: StarterDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db =
            Room
                .inMemoryDatabaseBuilder(context, StarterDatabase::class.java)
                .allowMainThreadQueries()
                .build()
    }

    @After
    fun tearDown() = db.close()

    private fun TestScope.repository(
        api: SampleApi,
        settings: SettingsDataSource,
        dao: SampleItemDao = db.sampleItemDao(),
    ) = SampleRepositoryImpl(
        database = db,
        api = api,
        dao = dao,
        settings = settings,
        ioDispatcher = StandardTestDispatcher(testScheduler),
        clock = { NOW },
    )

    private fun TestScope.settings() =
        SettingsDataSource(
            PreferenceDataStoreFactory.create(scope = backgroundScope) {
                File(tmp.root, "settings.preferences_pb")
            },
        )

    @Test
    fun `refresh success caches items and records time`() =
        runTest {
            db.sampleItemDao().upsertAll(listOf(SampleItemEntity(9, "stale")))
            val api =
                FakeApi { listOf(SampleItemResponse(2, "b"), SampleItemResponse(1, "a")) }
            val repository = repository(api, settings())

            val result = repository.refresh()

            assertEquals(AppResult.Success(Unit), result)
            assertEquals(listOf(SampleItem(1, "a"), SampleItem(2, "b")), repository.observeItems().first())
            assertEquals(NOW, repository.observeLastRefreshedAt().first())
        }

    @Test
    fun `refresh failure keeps old cache and returns Failure`() =
        runTest {
            db.sampleItemDao().upsertAll(listOf(SampleItemEntity(9, "old")))
            val failure = IllegalStateException("offline")
            val repository = repository(FakeApi { throw failure }, settings())

            val result = repository.refresh()

            assertEquals(failure, assertIs<AppResult.Failure>(result).error)
            assertEquals(listOf(SampleItem(9, "old")), repository.observeItems().first())
            assertNull(repository.observeLastRefreshedAt().first())
        }

    @Test
    fun `HTTP error status becomes HttpStatusException`() =
        runTest {
            val api = FakeApi { throw HttpException(Response.error<Unit>(503, "".toResponseBody())) }
            val repository = repository(api, settings())

            val error = assertIs<AppResult.Failure>(repository.refresh()).error

            assertEquals(503, assertIs<HttpStatusException>(error).code)
        }

    @Test
    fun `refresh rolls back clear when write fails`() =
        runTest {
            val real = db.sampleItemDao()
            real.upsertAll(listOf(SampleItemEntity(9, "old")))
            val failure = IllegalStateException("disk full")
            val failingDao =
                object : SampleItemDao by real {
                    override suspend fun upsertAll(items: List<SampleItemEntity>): Unit = throw failure
                }
            val api = FakeApi { listOf(SampleItemResponse(1, "a")) }
            val repository = repository(api, settings(), failingDao)

            val result = repository.refresh()

            // Coroutine stack-trace recovery may copy the exception, so compare type and message.
            val error = assertIs<AppResult.Failure>(result).error
            assertIs<IllegalStateException>(error)
            assertEquals("disk full", error.message)
            assertEquals(listOf(SampleItem(9, "old")), repository.observeItems().first())
            assertNull(repository.observeLastRefreshedAt().first())
        }

    private class FakeApi(
        private val block: () -> List<SampleItemResponse>,
    ) : SampleApi {
        override suspend fun getItems(): List<SampleItemResponse> = block()
    }

    private companion object {
        const val NOW = 1_700_000_000_000L
    }
}
