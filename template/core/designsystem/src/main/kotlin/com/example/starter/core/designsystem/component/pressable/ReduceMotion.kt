package com.example.starter.core.designsystem.component.pressable

import android.content.ContentResolver
import android.content.Context
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Watches the system "Remove animations" setting (animator duration scale 0) with one observer for the whole app.
 * One observer per pressable would repeat register/unregister binder calls on the main thread while a list scrolls,
 * so elements only read [flow]. The observer is registered on first use and never removed: it lives as long as the process.
 */
internal object ReduceMotion {
    private val state = MutableStateFlow(false)
    private var resolver: ContentResolver? = null
    private var observer: ContentObserver? = null

    fun flow(context: Context): StateFlow<Boolean> {
        val appResolver = context.applicationContext.contentResolver
        if (appResolver !== resolver) register(appResolver)
        return state
    }

    // In the app the resolver is set once. When the application context changes, as between tests, move to the new one.
    private fun register(appResolver: ContentResolver) {
        val newObserver =
            object : ContentObserver(Handler(Looper.getMainLooper())) {
                override fun onChange(selfChange: Boolean) {
                    state.value = appResolver.isReduceMotion()
                }
            }
        observer?.let { resolver?.unregisterContentObserver(it) }
        appResolver.registerContentObserver(Settings.Global.getUriFor(Settings.Global.ANIMATOR_DURATION_SCALE), false, newObserver)
        resolver = appResolver
        observer = newObserver
        state.value = appResolver.isReduceMotion()
    }

    private fun ContentResolver.isReduceMotion(): Boolean =
        Settings.Global.getFloat(this, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
}
