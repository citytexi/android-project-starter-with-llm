package com.example.starter.domain.usecase

import com.example.starter.core.common.AppResult
import com.example.starter.domain.repository.SampleRepository
import javax.inject.Inject

class RefreshSampleItemsUseCase
    @Inject
    constructor(
        private val repository: SampleRepository,
    ) {
        suspend operator fun invoke(): AppResult<Unit> = repository.refresh()
    }
