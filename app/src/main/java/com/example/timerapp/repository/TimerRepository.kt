package com.example.timerapp.repository

import androidx.lifecycle.LiveData
import com.example.timerapp.database.PresetTimer
import com.example.timerapp.database.Timer
import com.example.timerapp.database.TimerWithPresetTimer

// 実際に使用する用とtest用(今回はFakeRepository)でレポジトリが2つ必要なのでこのinterfaceを記述
interface TimerRepository {
    suspend fun insertTimer(timer: Timer)

    suspend fun insertPresetTimer(presetTimer: PresetTimer)

    suspend fun updateTimer(timer: Timer)

    suspend fun updatePresetTimer(presetTimer: PresetTimer)

    suspend fun deleteTimer(timer: Timer)

    suspend fun deletePresetTimer(presetTimer: PresetTimer)

    suspend fun getPresetTimerWithTimer(name: String): TimerWithPresetTimer

    suspend fun getCurrentTimer(name: String): Timer

    suspend fun getCurrentPresetTimer(timerName: String, presetName: String): PresetTimer

    suspend fun getNumberOfPresetTimers(name: String): Int

    suspend fun getTimerNames(): List<String>

    suspend fun getNumberOfTimers(): Int

    fun observeAllTimer(): LiveData<List<Timer>>

    // dao testに必要
    fun observeAllPresetTimer(): LiveData<List<PresetTimer>>
}