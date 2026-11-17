package com.example.starter.domain.usecase

import com.example.starter.domain.model.SampleItem
import com.example.starter.domain.repository.SampleRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveSampleItemsUseCase
    @Inject
    constructor(
        private val repository: SampleRepository,
    ) {
        operator fun invoke(): Flow<List<SampleItem>> = repository.observeItems()
    }
