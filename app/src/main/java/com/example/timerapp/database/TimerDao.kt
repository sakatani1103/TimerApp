package com.example.timerapp.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface TimerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimer(timer: Timer)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPresetTimer(presetTimer: PresetTimer)

    @Update
    suspend fun updateTimer(timer: Timer)

    @Update
    suspend fun updatePresetTimer(presetTimer: PresetTimer)

    @Delete
    suspend fun deleteTimer(timer: Timer)

    @Delete
    suspend fun deletePresetTimer(presetTimer: PresetTimer)

    @Transaction
    @Query("SELECT * FROM timer WHERE name = :name")
    suspend fun getPresetTimerWithTimer(name: String): TimerWithPresetTimer

    @Query("SELECT * FROM timer")
    fun observeAllTimer(): LiveData<List<Timer>>

    @Query("SELECT * FROM presetTimer")
    fun observeAllPresetTimer(): LiveData<List<PresetTimer>>

    @Query("SELECT * FROM timer WHERE name = :name ")
    fun getCurrentTimer(name: String) : Timer
}