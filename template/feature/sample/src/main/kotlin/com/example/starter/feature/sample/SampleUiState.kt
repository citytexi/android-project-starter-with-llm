package com.example.starter.feature.sample

import com.example.starter.domain.model.SampleItem

data class SampleUiState(
    val items: List<SampleItem> = emptyList(),
    val lastRefreshedAt: Long? = null,
    val isRefreshing: Boolean = false,
    val error: SampleError? = null,
)

/**
 * Why the last refresh failed. The ViewModel never holds display text: [SampleScreen] maps each
 * case to a string resource, so the message follows the device language.
 */
sealed interface SampleError {
    /** No connection, a timeout, or an HTTP error status (`IOException` from the data layer). */
    data object Network : SampleError

    /** Anything else. */
    data object Unknown : SampleError
}

sealed interface SampleAction {
    data object Refresh : SampleAction

    data object DismissError : SampleAction
}
