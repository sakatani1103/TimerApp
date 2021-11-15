package com.example.timerapp.di

import android.content.Context
import androidx.room.Room
import com.example.timerapp.database.TimerDao
import com.example.timerapp.database.TimerDatabase
import com.example.timerapp.repository.DefaultTimerRepository
import com.example.timerapp.repository.TimerRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// Hiltモジュール
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    // room database
    @Singleton
    @Provides
    fun provideTimerDatabase(
        @ApplicationContext context: Context
    ) = Room.databaseBuilder(context, TimerDatabase::class.java, "timer_db")
        .fallbackToDestructiveMigration()
        .build()

    // defaultTimerRepository
    @Singleton
    @Provides
    fun provideDefaultTimerRepository(
        dao: TimerDao,
    ) = DefaultTimerRepository(dao) as TimerRepository

    // dao
    @Singleton
    @Provides
    fun providesTimerDao(
        database: TimerDatabase
    ) = database.timerDao()
}