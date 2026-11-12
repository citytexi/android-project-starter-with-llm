package com.example.starter.domain.usecase

import com.example.starter.core.common.runSuspendCatching
import com.example.starter.domain.repository.SampleRepository
import javax.inject.Inject

/**
 * Repositories throw; UseCases turn the outcome into [Result] with [runSuspendCatching], even when
 * they only delegate. ViewModels call UseCases, never Repositories.
 */
class RefreshSampleItemsUseCase
    @Inject
    constructor(
        private val repository: SampleRepository,
    ) {
        suspend operator fun invoke(): Result<Unit> = runSuspendCatching { repository.refresh() }
    }
