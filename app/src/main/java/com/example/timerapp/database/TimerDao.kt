package com.example.timerapp.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface TimerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimer(timer: Timer)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPresetTimer(presettimer: PresetTimer)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPresetTimers(presettimers: List<PresetTimer>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimerAndPresetTimers(timer: Timer, presettimers: List<PresetTimer>)

    @Update
    suspend fun updateTimer(timer: Timer)

    @Update
    suspend fun updateTimers(timers: List<Timer>)

    @Update
    suspend fun updatePresetTimer(presettimer: PresetTimer)

    @Update
    suspend fun updatePresetTimers(presettimers: List<PresetTimer>)

    @Update
    suspend fun updateTimerAndPresetTimers(timer: Timer, presettimers: List<PresetTimer>)

    @Delete
    suspend fun deleteTimer(timer: Timer)

    @Delete
    suspend fun deletePresetTimer(presettimer: PresetTimer)

    @Delete
    suspend fun deletePresetTimers(presettimers: List<PresetTimer>)

    @Delete
    suspend fun deleteTimerAndPresetTimers(timer: Timer, presetTimers: List<PresetTimer>)

    @Transaction
    @Query("SELECT * FROM timer WHERE name = :name")
    suspend fun getPresetTimerWithTimer(name: String): TimerWithPresetTimer

    @Query("SELECT * FROM timer")
    fun observeAllTimer(): LiveData<List<Timer>>

    @Query("SELECT * FROM presettimer ORDER BY timerOrder ASC")
    fun observeAllPresetTimer(): LiveData<List<PresetTimer>>

    @Query("SELECT * FROM presettimer WHERE presetTimerId = :presetTimerId")
    suspend fun getCurrentPresetTimer(presetTimerId: String): PresetTimer

    @Query("SELECT name FROM timer")
    suspend fun getTimerNames(): List<String>
}