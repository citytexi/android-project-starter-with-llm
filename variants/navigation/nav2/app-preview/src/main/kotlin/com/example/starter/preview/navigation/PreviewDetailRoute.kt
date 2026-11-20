package com.example.starter.preview.navigation

import kotlinx.serialization.Serializable

/** Route of a catalog screen, found by its unique [title]. */
@Serializable
data class PreviewDetailRoute(
    val title: String,
)
