package com.example.starter.preview

import android.graphics.Color
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.LocalActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import com.example.starter.core.designsystem.theme.StarterTheme
import com.example.starter.preview.catalog.PreviewCatalogItem
import com.example.starter.preview.catalog.previewCatalog
import com.example.starter.preview.navigation.PreviewNavHost
import com.example.starter.preview.settings.PreviewControlBar
import com.example.starter.preview.settings.PreviewFontScale
import com.example.starter.preview.settings.PreviewThemeMode

/**
 * Root of the preview app. The control bar sits outside [PreviewNavHost] so one theme and font
 * scale apply to every screen without passing them down. [catalog] is a parameter only so tests
 * can open a screen of their own.
 */
@Composable
fun PreviewApp(catalog: List<PreviewCatalogItem> = previewCatalog) {
    var themeMode by rememberSaveable { mutableStateOf(PreviewThemeMode.SYSTEM) }
    var fontScale by rememberSaveable { mutableStateOf(PreviewFontScale.NORMAL) }
    val isDark =
        when (themeMode) {
            PreviewThemeMode.LIGHT -> false
            PreviewThemeMode.DARK -> true
            PreviewThemeMode.SYSTEM -> isSystemInDarkTheme()
        }

    val activity = LocalActivity.current as? ComponentActivity
    // SystemBarStyle.auto reads its dark-mode lambda only when enableEdgeToEdge runs, so re-run it per theme.
    DisposableEffect(activity, isDark) {
        activity?.enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { isDark },
            navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { isDark },
        )
        onDispose { }
    }

    val density = LocalDensity.current
    CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale = fontScale.scale)) {
        StarterTheme(darkTheme = isDark) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = StarterTheme.colors.bg.layerDefault,
                contentColor = StarterTheme.colors.fg.neutral,
            ) {
                Column(
                    Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.safeDrawing.exclude(WindowInsets.navigationBars)),
                ) {
                    PreviewNavHost(catalog = catalog, modifier = Modifier.fillMaxWidth().weight(1f))
                    PreviewControlBar(
                        themeMode = themeMode,
                        onThemeModeChange = { themeMode = it },
                        fontScale = fontScale,
                        onFontScaleChange = { fontScale = it },
                    )
                }
            }
        }
    }
}
