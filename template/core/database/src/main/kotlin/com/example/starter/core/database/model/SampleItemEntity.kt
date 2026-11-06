package com.example.starter.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sample_item")
data class SampleItemEntity(
    @PrimaryKey val id: Long,
    val title: String,
)
