package com.example.starter.feature.sample.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.starter.core.navigation.SampleNavRoute
import com.example.starter.feature.sample.SampleRoute

/** Registers the sample screen for [SampleNavRoute]. */
fun NavGraphBuilder.sampleScreen() {
    composable<SampleNavRoute> { SampleRoute() }
}
