package com.example.starter.feature.sample

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.starter.core.designsystem.component.StarterButton
import com.example.starter.core.designsystem.theme.StarterTheme
import com.example.starter.core.ui.LoadingContent
import com.example.starter.domain.model.SampleItem
import java.text.DateFormat
import java.util.Date

/** Stateless sample screen. Screenshot tests render this directly. */
@Composable
fun SampleScreen(
    state: SampleUiState,
    snackbarHostState: SnackbarHostState,
    onIntent: (SampleIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(modifier = modifier, snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.sample_title),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.semantics { heading() },
            )
            LastRefreshed(epochMillis = state.lastRefreshedAt)
            StarterButton(
                text = stringResource(R.string.sample_refresh),
                onClick = { onIntent(SampleIntent.Refresh) },
                enabled = !state.isRefreshing,
            )
            // The list stays on screen while refreshing; the spinner only covers an empty first load.
            LoadingContent(
                isLoading = state.isRefreshing,
                hasContent = state.items.isNotEmpty(),
                modifier = Modifier.fillMaxWidth().weight(1f),
            ) {
                SampleItems(items = state.items)
            }
        }
    }
}

/** Message for a [SampleSideEffect.ShowError] snackbar. The ViewModel never holds display text. */
@get:StringRes
internal val SampleError.messageRes: Int
    get() =
        when (this) {
            SampleError.Network -> R.string.sample_error_network
            SampleError.Server -> R.string.sample_error_server
            SampleError.Unknown -> R.string.sample_error_unknown
        }

@Composable
private fun LastRefreshed(epochMillis: Long?) {
    // DateFormat is costly to create: build it once per locale, not on every recomposition.
    val locale = LocalConfiguration.current.locales[0]
    val dateFormat = remember(locale) { DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, locale) }
    Text(
        text =
            epochMillis?.let { stringResource(R.string.sample_last_refreshed, dateFormat.format(Date(it))) }
                ?: stringResource(R.string.sample_never_refreshed),
        style = MaterialTheme.typography.bodyMedium,
    )
}

@Composable
private fun SampleItems(items: List<SampleItem>) {
    if (items.isEmpty()) {
        Text(text = stringResource(R.string.sample_empty), style = MaterialTheme.typography.bodyMedium)
        return
    }
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(items = items, key = { it.id }) { item ->
            Text(text = item.title, modifier = Modifier.padding(vertical = 12.dp))
            HorizontalDivider()
        }
    }
}

@Preview
@Composable
private fun SampleScreenPreview() {
    StarterTheme {
        SampleScreen(
            state = SampleUiState(items = listOf(SampleItem(1, "First"), SampleItem(2, "Second"))),
            snackbarHostState = remember { SnackbarHostState() },
            onIntent = {},
        )
    }
}
