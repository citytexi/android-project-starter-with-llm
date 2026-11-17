package com.example.starter.feature.sample.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.example.starter.core.navigation.SampleKey
import com.example.starter.feature.sample.SampleRoute

/** Registers the sample screen for [SampleKey]. */
fun EntryProviderScope<NavKey>.sampleEntry() {
    entry<SampleKey> { SampleRoute() }
}
