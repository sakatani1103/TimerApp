package com.example.timerapp

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.room.Room
import com.example.timerapp.database.TimerDatabase
import com.example.timerapp.repository.DefaultTimerRepository
import com.example.timerapp.repository.TimerRepository

object ServiceLocator {

    private var database: TimerDatabase? = null
    private var lock = Any()

    @Volatile
    var timerRepository: TimerRepository? = null
        @VisibleForTesting set

    fun provideTimerRepository(context: Context): TimerRepository {
        synchronized(this) {
            return timerRepository ?: createTimerRepository(context)
        }
    }

    private fun createTimerRepository(context: Context) : TimerRepository {
        val database = database ?: createDatabase(context)
        val repository = DefaultTimerRepository(database.timerDao())
        return repository
    }

    private fun createDatabase(context: Context): TimerDatabase {
        val result = Room.databaseBuilder(
            context.applicationContext,
            TimerDatabase::class.java, "Timer.db"
        )
            .fallbackToDestructiveMigration()
            .build()
        database = result
        return result
    }

    @VisibleForTesting
    fun resetRepository() {
        synchronized(lock){
            database?.apply {
                clearAllTables()
                close()
            }
            database = null
            timerRepository = null
        }
    }
}