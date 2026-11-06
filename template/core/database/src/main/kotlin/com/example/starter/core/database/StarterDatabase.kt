package com.example.starter.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.starter.core.database.dao.SampleItemDao
import com.example.starter.core.database.model.SampleItemEntity

@Database(entities = [SampleItemEntity::class], version = 1, exportSchema = true)
abstract class StarterDatabase : RoomDatabase() {
    abstract fun sampleItemDao(): SampleItemDao
}
