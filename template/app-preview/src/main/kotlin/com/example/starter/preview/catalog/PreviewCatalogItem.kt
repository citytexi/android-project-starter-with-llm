package com.example.starter.preview.catalog

import androidx.compose.runtime.Composable

/**
 * One screen of the catalog: a row on the home and the detail it opens. [title] identifies the
 * screen in the back stack (nav2 route or nav3 key), so it must be unique within the catalog.
 * [content] draws the detail; `onBack` returns to the home.
 */
class PreviewCatalogItem(
    val category: PreviewCategory,
    val title: String,
    val content: @Composable (onBack: () -> Unit) -> Unit,
)
