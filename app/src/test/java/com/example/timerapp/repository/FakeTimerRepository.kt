package com.example.timerapp.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.timerapp.database.PresetTimer
import com.example.timerapp.database.Timer
import com.example.timerapp.database.TimerDao
import com.example.timerapp.database.TimerWithPresetTimer
import javax.inject.Inject


class FakeTimerRepository: TimerRepository {
    private val timerItems = mutableListOf<Timer>()
    private val observableTimerItems = MutableLiveData<List<Timer>>()
    private val presetTimerItems = mutableListOf<PresetTimer>()
    private val observablePresetTimerItems = MutableLiveData<List<PresetTimer>>()
    private lateinit var currentTimer: Timer

    // mutableLiveData.postValueは毎回更新されるので作成したリストを加える
    override suspend fun insertTimer(timer: Timer) {
        timerItems.add(timer)
        observableTimerItems.postValue(timerItems)
    }

    override suspend fun insertPresetTimer(presetTimer: PresetTimer) {
        presetTimerItems.add(presetTimer)
        observablePresetTimerItems.postValue(presetTimerItems)
    }

    override suspend fun updateTimer(timer: Timer) {
        val timerIndex = timerItems.indexOf(timer)
        timerItems[timerIndex] = timer
        observableTimerItems.postValue(timerItems)
    }

    override suspend fun updatePresetTimer(presetTimer: PresetTimer) {
        val presetTimerIndex = presetTimerItems.indexOf(presetTimer)
        presetTimerItems[presetTimerIndex] = presetTimer
        observablePresetTimerItems.postValue(presetTimerItems)
    }

    override suspend fun deleteTimer(timer: Timer) {
        timerItems.remove(timer)
        observableTimerItems.postValue(timerItems)
    }

    override suspend fun deletePresetTimer(presetTimer: PresetTimer) {
        presetTimerItems.remove(presetTimer)
        observablePresetTimerItems.postValue(presetTimerItems)
    }

    override suspend fun getPresetTimerWithTimer(name: String): TimerWithPresetTimer {
        val timer = timerItems.first { it.name == name }
        val presetTimerList = presetTimerItems.filter { it.name == name }
        return TimerWithPresetTimer(timer, presetTimerList)
    }

    override suspend fun getCurrentTimer(name: String): Timer {
        currentTimer = timerItems.first { it.name == name }
//        for (i in timerItems.indices){
//            val timerName = timerItems[i].name
//            if (timerName == name) {
//                currentTimer = timerItems[i]
//            }
//        }
        return currentTimer
    }

    override fun observeAllTimer(): LiveData<List<Timer>> {
        return observableTimerItems
    }

    override fun observeAllPresetTimer(): LiveData<List<PresetTimer>> {
        return observablePresetTimerItems
    }

}

