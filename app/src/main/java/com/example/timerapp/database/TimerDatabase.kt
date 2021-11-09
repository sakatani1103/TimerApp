package com.example.timerapp.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        Timer::class,
        PresetTimer::class
    ],
    version = 1,
)
abstract class TimerDatabase : RoomDatabase() {
    // HiltModuleに記述するためfunctionにする
    abstract fun timerDao(): TimerDao
}