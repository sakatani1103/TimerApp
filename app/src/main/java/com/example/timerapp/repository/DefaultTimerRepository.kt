package com.example.timerapp.repository

import androidx.lifecycle.LiveData
import androidx.room.Query
import com.example.timerapp.database.PresetTimer
import com.example.timerapp.database.Timer
import com.example.timerapp.database.TimerDao
import com.example.timerapp.database.TimerWithPresetTimer
import javax.inject.Inject
import kotlin.concurrent.timer

// repositoryにRoomDaoをinject
// injectしたrepositoryの
class DefaultTimerRepository @Inject constructor(
    private val timerDao: TimerDao
): TimerRepository{
    override suspend fun insertTimer(timer: Timer) {
        timerDao.insertTimer(timer)
    }

    override suspend fun insertPresetTimer(presetTimer: PresetTimer) {
        timerDao.insertPresetTimer(presetTimer)
    }

    override suspend fun updateTimer(timer: Timer) {
        timerDao.updateTimer(timer)
    }

    override suspend fun updatePresetTimer(presetTimer: PresetTimer) {
        timerDao.updatePresetTimer(presetTimer)
    }

    override suspend fun deleteTimer(timer: Timer) {
        timerDao.deleteTimer(timer)
    }

    override suspend fun deletePresetTimer(presetTimer: PresetTimer) {
        timerDao.deletePresetTimer(presetTimer)
    }

    override suspend fun getPresetTimerWithTimer(name: String): TimerWithPresetTimer {
        return timerDao.getPresetTimerWithTimer(name)
    }

    override suspend fun getCurrentTimer(name: String): Timer {
        return timerDao.getCurrentTimer(name)
    }

    override suspend fun getCurrentPresetTimer(timerName: String, presetName: String): PresetTimer {
        return timerDao.getCurrentPresetTimer(timerName, presetName)
    }

    override suspend fun getNumberOfPresetTimers(name: String): Int {
        return timerDao.getNumberOfPresetTimers(name)
    }


    override fun observeAllTimer(): LiveData<List<Timer>> {
        return timerDao.observeAllTimer()
    }

    override fun observeAllPresetTimer(): LiveData<List<PresetTimer>> {
        return timerDao.observeAllPresetTimer()
    }

    override suspend fun getNumberOfTimers(): Int {
        return timerDao.getNumberOfTimers()
    }

    override suspend fun getTimerNames(): List<String> {
        return timerDao.getTimerNames()
    }


}