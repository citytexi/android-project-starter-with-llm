package com.example.starter.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.starter.core.database.model.SampleItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SampleItemDao {
    @Query("SELECT * FROM sample_item ORDER BY id")
    fun observeAll(): Flow<List<SampleItemEntity>>

    @Upsert
    suspend fun upsertAll(items: List<SampleItemEntity>)

    @Query("DELETE FROM sample_item")
    suspend fun clear()
}
