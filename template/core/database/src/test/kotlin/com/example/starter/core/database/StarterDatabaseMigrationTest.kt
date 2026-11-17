package com.example.starter.core.database

import android.content.Context
import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import com.example.starter.core.database.model.SampleItemEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals

/**
 * Builds old database versions from the exported schemas (`core/database/schemas/`) and opens them
 * with the current [StarterDatabase]. When the version goes up, Room runs the migrations on open,
 * so a missing or broken migration fails here instead of on users' devices.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class StarterDatabaseMigrationTest {
    @get:Rule
    val helper = MigrationTestHelper(InstrumentationRegistry.getInstrumentation(), StarterDatabase::class.java)

    @Test
    fun `version 1 opens with the current database and keeps rows`() =
        runTest {
            helper.createDatabase(DB_NAME, 1).use { db ->
                db.execSQL("INSERT INTO sample_item (id, title) VALUES (1, 'kept')")
            }

            val context = ApplicationProvider.getApplicationContext<Context>()
            val database =
                Room
                    .databaseBuilder(context, StarterDatabase::class.java, DB_NAME)
                    .allowMainThreadQueries()
                    .build()
            try {
                assertEquals(listOf(SampleItemEntity(1, "kept")), database.sampleItemDao().observeAll().first())
            } finally {
                database.close()
            }
        }

    private companion object {
        const val DB_NAME = "migration-test.db"
    }
}
