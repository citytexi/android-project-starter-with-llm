package com.example.starter.core.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.example.starter.core.database.dao.SampleItemDao
import com.example.starter.core.database.model.SampleItemEntity
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SampleItemDaoTest {
    private lateinit var db: StarterDatabase
    private lateinit var dao: SampleItemDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db =
            Room
                .inMemoryDatabaseBuilder(context, StarterDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        dao = db.sampleItemDao()
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun `upsertAll then observeAll emits items ordered by id`() =
        runTest {
            dao.upsertAll(listOf(SampleItemEntity(2, "b"), SampleItemEntity(1, "a")))

            dao.observeAll().test {
                assertEquals(listOf(SampleItemEntity(1, "a"), SampleItemEntity(2, "b")), awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `clear removes all`() =
        runTest {
            dao.upsertAll(listOf(SampleItemEntity(1, "a")))
            dao.clear()

            dao.observeAll().test {
                assertEquals(emptyList(), awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }
}
