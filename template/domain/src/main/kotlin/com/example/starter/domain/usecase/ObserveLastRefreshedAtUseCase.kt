package com.example.starter.domain.usecase

import com.example.starter.domain.repository.SampleRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Epoch millis of the last successful refresh, or `null` if it never succeeded. */
class ObserveLastRefreshedAtUseCase
    @Inject
    constructor(
        private val repository: SampleRepository,
    ) {
        operator fun invoke(): Flow<Long?> = repository.observeLastRefreshedAt()
    }
