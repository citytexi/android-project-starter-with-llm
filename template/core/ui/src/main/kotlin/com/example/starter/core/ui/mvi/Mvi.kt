package com.example.starter.core.ui.mvi

/** Everything a screen shows. An immutable `data class`; change it only through [MviContainer.updateState]. */
interface UiState

/** What the user did (or what the screen asks for). Passed to the ViewModel's `onIntent`. */
interface UiIntent

/** A one-off event the screen handles once, such as a snackbar or navigation. See [MviContainer.sideEffect]. */
interface UiSideEffect
