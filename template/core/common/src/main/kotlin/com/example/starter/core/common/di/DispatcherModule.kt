package com.example.starter.core.common.di

import com.example.starter.core.common.Dispatcher
import com.example.starter.core.common.StarterDispatcher
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Binds the [Dispatcher] qualifiers. It lives next to them in `core:common` (not in `data`), so
 * any module that injects a dispatcher, such as `core:datastore`, works without `data`.
 */
@Module
@InstallIn(SingletonComponent::class)
object DispatcherModule {
    @Provides
    @Dispatcher(StarterDispatcher.IO)
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @Dispatcher(StarterDispatcher.Default)
    fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default
}
