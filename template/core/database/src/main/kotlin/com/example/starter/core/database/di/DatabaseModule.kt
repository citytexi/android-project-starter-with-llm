package com.example.starter.core.database.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import com.example.starter.core.database.StarterDatabase
import com.example.starter.core.database.dao.SampleItemDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): StarterDatabase = Room.databaseBuilder(context, StarterDatabase::class.java, "starter.db").build()

    @Provides
    fun provideSampleItemDao(database: StarterDatabase): SampleItemDao = database.sampleItemDao()
}
