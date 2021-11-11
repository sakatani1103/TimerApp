package com.example.timerapp.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface TimerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimer(timer: Timer)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPresetTimer(presettimer: PresetTimer)

    @Update
    suspend fun updateTimer(timer: Timer)

    @Update
    suspend fun updatePresetTimer(presettimer: PresetTimer)

    @Delete
    suspend fun deleteTimer(timer: Timer)

    @Delete
    suspend fun deletePresetTimer(presettimer: PresetTimer)

    @Transaction
    @Query("SELECT * FROM timer WHERE name = :name")
    suspend fun getPresetTimerWithTimer(name: String): TimerWithPresetTimer

    @Query("SELECT * FROM timer")
    fun observeAllTimer(): LiveData<List<Timer>>

    @Query("SELECT * FROM presettimer")
    fun observeAllPresetTimer(): LiveData<List<PresetTimer>>

    @Query("SELECT name FROM timer")
    fun observeAllTimerName(): LiveData<List<String>>

    @Query("SELECT COUNT(*) FROM timer")
    fun observeNumberOfTimer(): LiveData<Int>

    @Query("SELECT * FROM timer WHERE name = :name ")
    suspend fun getCurrentTimer(name: String) : Timer

    @Query("SELECT * FROM timer WHERE name = :name")
    fun observeCurrentTimer(name: String) : LiveData<Timer>

    @Query("SELECT * FROM presettimer WHERE presetTimerId = :id")
    suspend fun getCurrentPresetTimer(id: Long) : PresetTimer

    @Query("SELECT COUNT(*) FROM presettimer WHERE name = :name")
    suspend fun getNumberOfPresetTimers(name: String) : Int
}