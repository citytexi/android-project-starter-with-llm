package com.example.starter.preview.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/** Back stack key of a catalog screen, found by its unique [title]. */
@Serializable
data class PreviewDetailKey(
    val title: String,
) : NavKey
