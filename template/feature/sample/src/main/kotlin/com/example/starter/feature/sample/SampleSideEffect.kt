package com.example.starter.feature.sample

import com.example.starter.core.ui.mvi.UiSideEffect

sealed interface SampleSideEffect : UiSideEffect {
    /** A refresh failed. The Route shows it in a snackbar; the list stays as it was. */
    data class ShowError(
        val error: SampleError,
    ) : SampleSideEffect
}
