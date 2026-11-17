package com.example.starter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.starter.core.designsystem.theme.StarterTheme
import com.example.starter.navigation.StarterNavHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        // Must run before super.onCreate so the splash theme swaps to postSplashScreenTheme.
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        splashScreen.setKeepOnScreenCondition { !viewModel.isReady.value }
        enableEdgeToEdge()
        setContent {
            StarterTheme {
                // Provided by the navigation variant (variants/navigation/<nav>/app/...).
                StarterNavHost()
            }
        }
    }
}
