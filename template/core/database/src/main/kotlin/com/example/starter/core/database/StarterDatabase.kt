package com.example.starter.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.starter.core.database.dao.SampleItemDao
import com.example.starter.core.database.model.SampleItemEntity

/**
 * App database. Every version's schema is exported to `core/database/schemas/` (commit it).
 *
 * To change an entity:
 * 1. Change the entity, raise [version] by one, and build once. Room writes `<new version>.json`.
 * 2. Add an automatic migration for simple changes (new table, new column with a default):
 *    ```
 *    @Database(
 *        entities = [...],
 *        version = 2,
 *        autoMigrations = [AutoMigration(from = 1, to = 2)],
 *    )
 *    ```
 *    Renames and deletes need an `AutoMigrationSpec` (`@RenameColumn`, `@DeleteColumn`, ...).
 *    Data rewrites need a manual `Migration(1, 2)` passed to `addMigrations` in `DatabaseModule`.
 * 3. `StarterDatabaseMigrationTest` builds the old version from its exported schema and opens it
 *    with the current database, so a missing migration fails the test. Add a case for each version.
 *
 * Never edit or delete an exported schema of a released version.
 */
@Database(entities = [SampleItemEntity::class], version = 1, exportSchema = true)
abstract class StarterDatabase : RoomDatabase() {
    abstract fun sampleItemDao(): SampleItemDao
}
