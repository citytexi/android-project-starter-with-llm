package com.example.starter.core.common

import javax.inject.Qualifier

/** Qualifier selecting a [StarterDispatcher]. Bindings live in [com.example.starter.core.common.di.DispatcherModule]. */
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class Dispatcher(
    val value: StarterDispatcher,
)
