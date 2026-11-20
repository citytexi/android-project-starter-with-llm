package com.example.starter.preview.navigation

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.starter.preview.catalog.PreviewCatalogItem
import com.example.starter.preview.home.PreviewHomeContent

/**
 * Preview navigation (nav2): the home, then one detail per catalog screen. Every navigation
 * variant provides this same function, so the rest of the module does not know which is active.
 */
@Composable
fun PreviewNavHost(
    catalog: List<PreviewCatalogItem>,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    // Pop only above the home: the exiting detail stays composed during the pop transition, so a
    // second tap on its back button must not pop the home and leave an empty host.
    val onBack: () -> Unit = {
        if (navController.previousBackStackEntry != null) navController.popBackStack()
    }
    // NavHost handles back only above the start destination; without this, back on the home
    // would only move the task to the background (Android 12+) instead of closing the app.
    val current by navController.currentBackStackEntryAsState()
    val activity = LocalActivity.current
    BackHandler(enabled = current?.destination?.hasRoute<PreviewHomeRoute>() == true) { activity?.finish() }

    NavHost(navController = navController, startDestination = PreviewHomeRoute, modifier = modifier) {
        composable<PreviewHomeRoute> {
            PreviewHomeContent(catalog = catalog, onOpen = { navController.navigate(PreviewDetailRoute(it.title)) })
        }
        // A route restored after the catalog lost that screen shows nothing; back still works.
        composable<PreviewDetailRoute> { entry ->
            val title = entry.toRoute<PreviewDetailRoute>().title
            catalog.firstOrNull { it.title == title }?.content?.invoke(onBack)
        }
    }
}
