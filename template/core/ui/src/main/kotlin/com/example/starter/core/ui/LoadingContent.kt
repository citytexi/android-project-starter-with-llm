package com.example.starter.core.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics

/**
 * Loading wrapper for content that refreshes in place. [modifier] sizes the wrapper in every state.
 *
 * - [isLoading] and not [hasContent]: a centered spinner replaces [content] (first load).
 * - [isLoading] and [hasContent]: [content] stays visible with a thin bar on top (refresh).
 * - otherwise: [content] only.
 *
 * Both indicators carry a "loading" content description for screen readers.
 */
@Composable
fun LoadingContent(
    isLoading: Boolean,
    hasContent: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val loadingLabel = stringResource(R.string.core_ui_loading)
    Box(modifier = modifier) {
        if (isLoading && !hasContent) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center).semantics { contentDescription = loadingLabel },
            )
        } else {
            content()
            if (isLoading) {
                LinearProgressIndicator(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter)
                            .semantics { contentDescription = loadingLabel },
                )
            }
        }
    }
}
