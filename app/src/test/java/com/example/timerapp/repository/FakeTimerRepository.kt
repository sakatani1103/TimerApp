package com.example.timerapp.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.timerapp.database.PresetTimer
import com.example.timerapp.database.Timer
import com.example.timerapp.database.TimerWithPresetTimer
import kotlinx.coroutines.runBlocking


class FakeTimerRepository: TimerRepository {
    private val timerItems = mutableListOf<Timer>()
    private val observableTimerItems = MutableLiveData<List<Timer>>()

    private val presetTimerItems = mutableListOf<PresetTimer>()
    private val observablePresetTimerItems = MutableLiveData<List<PresetTimer>>()

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
        return timerItems.first { it.name == name }
    }

    override suspend fun getCurrentPresetTimer(timerName: String, presetName: String): PresetTimer {
        return presetTimerItems.first { it.name == timerName && it.presetName == presetName }
    }

    override suspend fun getNumberOfPresetTimers(name: String): Int {
        return presetTimerItems.count{it.name == name}
    }

    override suspend fun getTimerNames(): List<String> {
        val names = mutableListOf<String>()
        timerItems.forEach {
            names.add(it.name)
        }
        return names
    }

    override suspend fun getNumberOfTimers(): Int {
        return timerItems.count()
    }

    override fun observeAllTimer(): LiveData<List<Timer>> {
        return observableTimerItems
    }

    override fun observeAllPresetTimer(): LiveData<List<PresetTimer>> {
        return observablePresetTimerItems
    }

    fun addTasks(vararg timers: Timer) {
        for (timer in timers){
            timerItems.add(timer)
        }
        runBlocking {
            observableTimerItems.value = timerItems
        }
    }
}

