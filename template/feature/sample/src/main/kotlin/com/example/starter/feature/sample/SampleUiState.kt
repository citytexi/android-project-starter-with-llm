package com.example.starter.feature.sample

import com.example.starter.core.ui.mvi.UiState
import com.example.starter.domain.model.SampleItem

data class SampleUiState(
    val items: List<SampleItem> = emptyList(),
    val lastRefreshedAt: Long? = null,
    val isRefreshing: Boolean = false,
) : UiState
