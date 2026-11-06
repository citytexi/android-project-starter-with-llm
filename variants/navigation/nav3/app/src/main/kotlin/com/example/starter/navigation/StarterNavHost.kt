package com.example.starter.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.example.starter.core.navigation.SampleKey
import com.example.starter.feature.sample.navigation.sampleEntry

/**
 * App navigation (nav3). Every navigation variant provides this same parameterless
 * function, so MainActivity does not know which variant is active.
 */
@Composable
fun StarterNavHost() {
    val backStack = rememberNavBackStack(SampleKey)
    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators =
            listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                // Scopes hiltViewModel() to each back stack entry.
                rememberViewModelStoreNavEntryDecorator(),
            ),
        entryProvider = entryProvider { sampleEntry() },
    )
}
