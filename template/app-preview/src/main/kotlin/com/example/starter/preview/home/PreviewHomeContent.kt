package com.example.starter.preview.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.starter.core.designsystem.theme.StarterPreviewTheme
import com.example.starter.core.designsystem.theme.StarterTheme
import com.example.starter.core.ui.preview.StarterPreview
import com.example.starter.preview.catalog.PreviewCatalogItem
import com.example.starter.preview.catalog.PreviewCategory

/** Catalog list grouped by [PreviewCategory]; draws no background of its own. */
@Composable
fun PreviewHomeContent(
    catalog: List<PreviewCatalogItem>,
    onOpen: (PreviewCatalogItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val gutter = StarterTheme.dimension.spacingX.globalGutter
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = StarterTheme.dimension.x4),
    ) {
        item {
            Text(
                text = "Starter Preview",
                modifier = Modifier.padding(horizontal = gutter),
                style = StarterTheme.typography.t9Bold,
                color = StarterTheme.colors.fg.neutral,
            )
        }
        if (catalog.isEmpty()) {
            item {
                Text(
                    text = "등록된 화면이 없습니다",
                    modifier = Modifier.padding(horizontal = gutter, vertical = StarterTheme.dimension.x3),
                    style = StarterTheme.typography.t5Regular,
                    color = StarterTheme.colors.fg.neutralSubtle,
                )
            }
        }
        PreviewCategory.entries.forEach { category ->
            val rows = catalog.filter { it.category == category }
            if (rows.isNotEmpty()) {
                item {
                    Text(
                        text = category.title(),
                        modifier =
                            Modifier.padding(
                                start = gutter,
                                end = gutter,
                                top = StarterTheme.dimension.x6,
                                bottom = StarterTheme.dimension.x2,
                            ),
                        style = StarterTheme.typography.t4Bold,
                        color = StarterTheme.colors.fg.neutralMuted,
                    )
                }
                items(rows, key = { it.title }) { row ->
                    Text(
                        text = row.title,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clickable { onOpen(row) }
                                .padding(horizontal = gutter, vertical = StarterTheme.dimension.x3),
                        style = StarterTheme.typography.t5Regular,
                        color = StarterTheme.colors.fg.neutral,
                    )
                }
            }
        }
    }
}

private fun PreviewCategory.title(): String =
    when (this) {
        PreviewCategory.FOUNDATION -> "Foundation"
        PreviewCategory.COMPONENT -> "Component"
    }

@StarterPreview
@Composable
private fun PreviewHomeContentPreview() {
    StarterPreviewTheme {
        PreviewHomeContent(
            catalog = listOf(PreviewCatalogItem(PreviewCategory.FOUNDATION, "Color") {}),
            onOpen = {},
        )
    }
}

@StarterPreview
@Composable
private fun PreviewHomeContentEmptyPreview() {
    StarterPreviewTheme {
        PreviewHomeContent(catalog = emptyList(), onOpen = {})
    }
}
