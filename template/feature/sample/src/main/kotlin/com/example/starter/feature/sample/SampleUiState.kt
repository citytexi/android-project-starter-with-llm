package com.example.starter.feature.sample

import com.example.starter.core.ui.mvi.UiIntent
import com.example.starter.core.ui.mvi.UiSideEffect
import com.example.starter.core.ui.mvi.UiState
import com.example.starter.domain.model.SampleItem

data class SampleUiState(
    val items: List<SampleItem> = emptyList(),
    val lastRefreshedAt: Long? = null,
    val isRefreshing: Boolean = false,
) : UiState

/**
 * Why the last refresh failed. The ViewModel never holds display text: [SampleScreen] maps each
 * case to a string resource, so the message follows the device language.
 */
sealed interface SampleError {
    /** No connection or a timeout (`IOException` from the data layer). */
    data object Network : SampleError

    /** The server answered with an HTTP error status (`HttpStatusException`). */
    data object Server : SampleError

    /** Anything else. */
    data object Unknown : SampleError
}

sealed interface SampleIntent : UiIntent {
    data object Refresh : SampleIntent
}

sealed interface SampleSideEffect : UiSideEffect {
    /** A refresh failed. The Route shows it in a snackbar; the list stays as it was. */
    data class ShowError(
        val error: SampleError,
    ) : SampleSideEffect
}
