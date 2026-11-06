package com.example.starter.domain.repository

import com.example.starter.core.common.AppResult
import com.example.starter.domain.model.SampleItem
import kotlinx.coroutines.flow.Flow

interface SampleRepository {
    fun observeItems(): Flow<List<SampleItem>>

    fun observeLastRefreshedAt(): Flow<Long?>

    suspend fun refresh(): AppResult<Unit>
}
