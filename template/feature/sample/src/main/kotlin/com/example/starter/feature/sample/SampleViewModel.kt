package com.example.starter.feature.sample

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.starter.core.common.AppResult
import com.example.starter.core.common.HttpStatusException
import com.example.starter.core.ui.mvi.MutableMviContainer
import com.example.starter.core.ui.mvi.MviContainer
import com.example.starter.domain.usecase.ObserveLastRefreshedAtUseCase
import com.example.starter.domain.usecase.ObserveSampleItemsUseCase
import com.example.starter.domain.usecase.RefreshSampleItemsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class SampleViewModel
    @Inject
    constructor(
        observeSampleItems: ObserveSampleItemsUseCase,
        observeLastRefreshedAt: ObserveLastRefreshedAtUseCase,
        private val refreshSampleItems: RefreshSampleItemsUseCase,
    ) : ViewModel() {
        private val _container = MutableMviContainer<SampleUiState, SampleSideEffect>(viewModelScope, SampleUiState())

        /** Read-only for the Route: state and side effects, no way to change them. */
        val container: MviContainer<SampleUiState, SampleSideEffect> = _container

        init {
            _container.launchWhileSubscribed(
                source = { combine(observeSampleItems(), observeLastRefreshedAt(), ::Pair) },
            ) { (items, lastRefreshedAt) ->
                _container.updateState { copy(items = items, lastRefreshedAt = lastRefreshedAt) }
            }
        }

        fun onIntent(intent: SampleIntent) {
            when (intent) {
                SampleIntent.Refresh -> refresh()
            }
        }

        private fun refresh() {
            // The key ignores a second Refresh while one is running.
            _container.launch(key = SampleIntent.Refresh) {
                _container.updateState { copy(isRefreshing = true) }
                val failure = refreshSampleItems() as? AppResult.Failure
                _container.updateState { copy(isRefreshing = false) }
                if (failure != null) {
                    _container.postSideEffect(SampleSideEffect.ShowError(failure.error.toSampleError()))
                }
            }
        }
    }

/**
 * The data layer reports network and HTTP failures as `IOException`. [HttpStatusException] is an
 * `IOException`, so it must be checked first. Everything else is [SampleError.Unknown].
 */
private fun Throwable.toSampleError(): SampleError =
    when (this) {
        is HttpStatusException -> SampleError.Server
        is IOException -> SampleError.Network
        else -> SampleError.Unknown
    }
