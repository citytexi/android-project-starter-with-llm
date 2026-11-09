package com.example.starter.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.example.starter.core.navigation.SampleNavRoute
import com.example.starter.feature.sample.navigation.sampleScreen

/**
 * App navigation (nav2). Every navigation variant provides this same parameterless
 * function, so MainActivity does not know which variant is active.
 */
@Composable
fun StarterNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = SampleNavRoute) {
        sampleScreen()
    }
}
