package com.example.starter.feature.sample

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalResources
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.starter.core.ui.mvi.collectAsStateWithLifecycle
import com.example.starter.core.ui.mvi.collectSideEffect

/** Stateful entry point used by navigation. Not screenshot-tested: it needs Hilt. */
@Composable
fun SampleRoute(viewModel: SampleViewModel = hiltViewModel()) {
    val state by viewModel.container.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val resources = LocalResources.current
    viewModel.container.collectSideEffect { effect ->
        when (effect) {
            is SampleSideEffect.ShowError ->
                snackbarHostState.showSnackbar(
                    message = resources.getString(effect.error.messageRes),
                    withDismissAction = true,
                )
        }
    }
    SampleScreen(state = state, snackbarHostState = snackbarHostState, onIntent = viewModel::onIntent)
}
