// One file for every route of the app, so the file name stays StarterRoutes.
@file:Suppress("MatchingDeclarationName", "ktlint:standard:filename")

package com.example.starter.core.navigation

import kotlinx.serialization.Serializable

// Type-safe Navigation 2 routes. They must be @Serializable: NavHost builds the route
// from the generated serializer. The Nav suffix keeps them apart from the feature's
// route composables (SampleRoute).

@Serializable
data object SampleNavRoute
