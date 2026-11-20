package com.example.starter.preview.navigation

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.example.starter.preview.catalog.PreviewCatalogItem
import com.example.starter.preview.home.PreviewHomeContent

/**
 * Preview navigation (nav3): the home, then one detail per catalog screen. Every navigation
 * variant provides this same function, so the rest of the module does not know which is active.
 */
@Composable
fun PreviewNavHost(
    catalog: List<PreviewCatalogItem>,
    modifier: Modifier = Modifier,
) {
    val backStack = rememberNavBackStack(PreviewHomeKey)
    // Pop only: the exiting detail stays composed during the pop transition, so a second tap on its
    // back button must not fall through to finish().
    val onBack: () -> Unit = {
        if (backStack.size > 1) backStack.removeLastOrNull()
    }
    // NavDisplay disables its own back handler on a single entry; without this, back on the home
    // would only move the task to the background (Android 12+) instead of closing the app.
    val activity = LocalActivity.current
    BackHandler(enabled = backStack.size == 1) { activity?.finish() }

    NavDisplay(
        backStack = backStack,
        modifier = modifier,
        onBack = onBack,
        entryDecorators = listOf(rememberSaveableStateHolderNavEntryDecorator()),
        entryProvider =
            entryProvider {
                entry<PreviewHomeKey> {
                    PreviewHomeContent(catalog = catalog, onOpen = { backStack.add(PreviewDetailKey(it.title)) })
                }
                // A key restored after the catalog lost that screen shows nothing; back still works.
                entry<PreviewDetailKey> { key -> catalog.firstOrNull { it.title == key.title }?.content?.invoke(onBack) }
            },
    )
}
