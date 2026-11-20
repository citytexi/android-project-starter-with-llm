package com.example.starter.feature.sample

import com.example.starter.core.ui.mvi.UiIntent

sealed interface SampleIntent : UiIntent {
    data object Refresh : SampleIntent
}
