package com.example.starter.core.common

import javax.inject.Qualifier

/** Coroutine dispatchers that can be injected through [Dispatcher]. */
enum class StarterDispatcher { IO, Default }

/** Qualifier selecting a [StarterDispatcher]. Bindings live in [com.example.starter.core.common.di.DispatcherModule]. */
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class Dispatcher(
    val value: StarterDispatcher,
)
