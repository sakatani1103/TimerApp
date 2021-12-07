package com.example.timerapp.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        Timer::class,
        PresetTimer::class
    ],
    version = 1,
)
abstract class TimerDatabase : RoomDatabase() {
    abstract fun timerDao(): TimerDao
}