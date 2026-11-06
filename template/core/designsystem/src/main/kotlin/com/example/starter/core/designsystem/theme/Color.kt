package com.example.starter.core.designsystem.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

internal val LightColorScheme =
    lightColorScheme(
        primary = Color(0xFF3F51B5),
        onPrimary = Color(0xFFFFFFFF),
        background = Color(0xFFFFFBFE),
        onBackground = Color(0xFF1C1B1F),
        surface = Color(0xFFFFFBFE),
        onSurface = Color(0xFF1C1B1F),
    )

internal val DarkColorScheme =
    darkColorScheme(
        primary = Color(0xFFB9C3FF),
        onPrimary = Color(0xFF0B1B7A),
        background = Color(0xFF1C1B1F),
        onBackground = Color(0xFFE6E1E5),
        surface = Color(0xFF1C1B1F),
        onSurface = Color(0xFFE6E1E5),
    )
