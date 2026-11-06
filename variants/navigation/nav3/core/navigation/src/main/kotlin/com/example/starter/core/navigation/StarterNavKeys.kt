// One file for every back stack key of the app, so the file name stays StarterNavKeys.
@file:Suppress("MatchingDeclarationName", "ktlint:standard:filename")

package com.example.starter.core.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

// Back stack keys. They must be @Serializable: rememberNavBackStack saves them across
// process death. app/proguard-rules.pro keeps NavKey classes for the reflective lookup.

@Serializable
data object SampleKey : NavKey
