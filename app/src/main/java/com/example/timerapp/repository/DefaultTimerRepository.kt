package com.example.timerapp.repository

import androidx.lifecycle.LiveData
import com.example.timerapp.database.*

class DefaultTimerRepository internal constructor( private val timerDao: TimerDao ) : TimerRepository {

    override suspend fun insertTimer(timer: Timer) {
        timerDao.insertTimer(timer)
    }

    override suspend fun insertPresetTimer(presetTimer: PresetTimer) {
        timerDao.insertPresetTimer(presetTimer)
    }

    override suspend fun insertPresetTimers(presetTimers: List<PresetTimer>) {
        timerDao.insertPresetTimers(presetTimers)
    }

    override suspend fun insertTimerAndPresetTimers(
        timer: Timer,
        presetTimers: List<PresetTimer>
    ) {
        timerDao.insertTimerAndPresetTimers(timer, presetTimers)
    }

    override suspend fun updateTimer(timer: Timer) {
        timerDao.updateTimer(timer)
    }

    override suspend fun updateTimers(timers: List<Timer>) {
        timerDao.updateTimers(timers)
    }

    override suspend fun updatePresetTimer(presetTimer: PresetTimer) {
        timerDao.updatePresetTimer(presetTimer)
    }

    override suspend fun updatePresetTimers(presetTimers: List<PresetTimer>) {
        timerDao.updatePresetTimers(presetTimers)
    }

    override suspend fun updateTimerAndPresetTimers(timer: Timer, presetTimers: List<PresetTimer>) {
        timerDao.updateTimerAndPresetTimers(timer, presetTimers)
    }

    override suspend fun deleteTimerAndPresetTimers(timer: Timer, presetTimers: List<PresetTimer>) {
        timerDao.deleteTimerAndPresetTimers(timer, presetTimers)
    }

    override suspend fun deleteTimer(timer: Timer) {
        timerDao.deleteTimer(timer)
    }

    override suspend fun deletePresetTimer(presetTimer: PresetTimer) {
        timerDao.deletePresetTimer(presetTimer)
    }

    override suspend fun deletePresetTimers(presetTimers: List<PresetTimer>) {
        timerDao.deletePresetTimers(presetTimers)
    }

    override suspend fun getPresetTimerWithTimer(name: String): TimerWithPresetTimer {
        return timerDao.getPresetTimerWithTimer(name)
    }

    override suspend fun getCurrentPresetTimer(presetTimerId: String): PresetTimer {
        return timerDao.getCurrentPresetTimer(presetTimerId)
    }

    override fun observeAllTimer(): LiveData<List<Timer>> {
        return timerDao.observeAllTimer()
    }

    override fun observeAllPresetTimer(): LiveData<List<PresetTimer>> {
        return timerDao.observeAllPresetTimer()
    }

    override suspend fun getTimerNames(): List<String> {
        return timerDao.getTimerNames()
    }

}