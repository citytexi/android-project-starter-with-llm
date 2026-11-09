package com.example.starter.feature.sample

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.starter.core.common.AppResult
import com.example.starter.core.common.HttpStatusException
import com.example.starter.domain.usecase.ObserveLastRefreshedAtUseCase
import com.example.starter.domain.usecase.ObserveSampleItemsUseCase
import com.example.starter.domain.usecase.RefreshSampleItemsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
        private val refreshState = MutableStateFlow(RefreshState())

        val uiState: StateFlow<SampleUiState> =
            combine(observeSampleItems(), observeLastRefreshedAt(), refreshState) { items, lastRefreshedAt, refresh ->
                SampleUiState(
                    items = items,
                    lastRefreshedAt = lastRefreshedAt,
                    isRefreshing = refresh.isRefreshing,
                    error = refresh.error,
                )
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), SampleUiState())

        fun onAction(action: SampleAction) {
            when (action) {
                SampleAction.Refresh -> refresh()
                SampleAction.DismissError -> refreshState.update { it.copy(error = null) }
            }
        }

        private fun refresh() {
            if (refreshState.value.isRefreshing) return
            refreshState.update { it.copy(isRefreshing = true, error = null) }
            viewModelScope.launch {
                val failure = refreshSampleItems() as? AppResult.Failure
                refreshState.value =
                    RefreshState(
                        isRefreshing = false,
                        error = failure?.error?.toSampleError(),
                    )
            }
        }

        private data class RefreshState(
            val isRefreshing: Boolean = false,
            val error: SampleError? = null,
        )

        private companion object {
            const val STOP_TIMEOUT_MILLIS = 5_000L
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
