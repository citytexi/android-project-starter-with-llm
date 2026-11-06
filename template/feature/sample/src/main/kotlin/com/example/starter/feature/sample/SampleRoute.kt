package com.example.starter.feature.sample

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/** Stateful entry point used by navigation. Not screenshot-tested: it needs Hilt. */
@Composable
fun SampleRoute(viewModel: SampleViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    SampleScreen(uiState = uiState, onAction = viewModel::onAction)
}
