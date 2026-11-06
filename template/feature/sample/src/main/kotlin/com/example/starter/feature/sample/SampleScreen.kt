package com.example.starter.feature.sample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
    uiState: SampleUiState,
    onAction: (SampleAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(modifier = modifier) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(text = stringResource(R.string.sample_title), style = MaterialTheme.typography.headlineSmall)
            Text(
                text =
                    uiState.lastRefreshedAt?.let { stringResource(R.string.sample_last_refreshed, formatTime(it)) }
                        ?: stringResource(R.string.sample_never_refreshed),
                style = MaterialTheme.typography.bodyMedium,
            )
            StarterButton(
                text = stringResource(R.string.sample_refresh),
                onClick = { onAction(SampleAction.Refresh) },
                enabled = !uiState.isRefreshing,
            )
            uiState.errorMessage?.let { message ->
                ErrorBanner(message = message, onDismiss = { onAction(SampleAction.DismissError) })
            }
            LoadingContent(isLoading = uiState.isRefreshing) {
                SampleItems(items = uiState.items)
            }
        }
    }
}

@Composable
private fun ErrorBanner(
    message: String,
    onDismiss: () -> Unit,
) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
        )
        TextButton(onClick = onDismiss) { Text(text = stringResource(R.string.sample_dismiss)) }
    }
}

@Composable
private fun SampleItems(items: List<SampleItem>) {
    if (items.isEmpty()) {
        Text(text = stringResource(R.string.sample_empty), style = MaterialTheme.typography.bodyMedium)
        return
    }
    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        items(items = items, key = { it.id }) { item ->
            Text(text = item.title, modifier = Modifier.padding(vertical = 12.dp))
            HorizontalDivider()
        }
    }
}

private fun formatTime(epochMillis: Long): String = DateFormat.getDateTimeInstance().format(Date(epochMillis))

@Preview
@Composable
private fun SampleScreenPreview() {
    StarterTheme {
        SampleScreen(
            uiState = SampleUiState(items = listOf(SampleItem(1, "First"), SampleItem(2, "Second"))),
            onAction = {},
        )
    }
}
