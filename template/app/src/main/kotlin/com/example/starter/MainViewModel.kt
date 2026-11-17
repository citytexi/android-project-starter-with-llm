package com.example.starter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.starter.core.datastore.SettingsDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import javax.inject.Inject

/** Holds the splash screen until the settings DataStore has loaded once. */
@HiltViewModel
class MainViewModel
    @Inject
    constructor(
        settings: SettingsDataSource,
    ) : ViewModel() {
        // Eagerly: the splash condition reads `value` without collecting. A failed read
        // still ends the splash, so the app never hangs on it.
        val isReady: StateFlow<Boolean> =
            settings.lastRefreshedAt
                .take(1)
                .map { true }
                .catch { emit(true) }
                .stateIn(viewModelScope, SharingStarted.Eagerly, false)
    }
