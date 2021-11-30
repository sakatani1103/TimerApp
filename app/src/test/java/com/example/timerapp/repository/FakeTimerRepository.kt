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

    override suspend fun insertPresetTimers(presetTimers: List<PresetTimer>) {
        presetTimers.forEach { presetTimer -> presetTimerItems.add(presetTimer) }
        observablePresetTimerItems.postValue(presetTimerItems)
    }

    override suspend fun insertTimerAndPresetTimers(timer: Timer, presetTimers: List<PresetTimer>) {
        timerItems.add(timer)
        observableTimerItems.postValue(timerItems)
        presetTimers.forEach { presetTimer -> presetTimerItems.add(presetTimer) }
        observablePresetTimerItems.postValue(presetTimers)
    }

    override suspend fun updateTimer(timer: Timer) {
        var updateIndex = 0
        timerItems.forEachIndexed { index, it -> if (it.name == timer.name) updateIndex = index }
        timerItems[updateIndex] = timer
        observableTimerItems.postValue(timerItems)
    }

    override suspend fun updateTimers(timers: List<Timer>) {
        timers.forEach { timer ->
            var updateIndex = 0
            timerItems.forEachIndexed { index, it -> if (it.name == timer.name) updateIndex = index }
            timerItems[updateIndex] = timer
        }
        observableTimerItems.postValue(timerItems)
    }

    override suspend fun updatePresetTimers(presetTimers: List<PresetTimer>) {
        presetTimers.forEach { presetTimer ->
            val target = presetTimerItems.filter {
                it.name == presetTimer.name && it.presetName == presetTimer.presetName && it.timerOrder == presetTimer.timerOrder}
            val index = presetTimerItems.indexOf(target.first())
            presetTimerItems[index] = presetTimer
        }

        observablePresetTimerItems.postValue(presetTimerItems)
    }

    override suspend fun updateTimerAndPresetTimers(timer: Timer, presetTimers: List<PresetTimer>) {
        updateTimer(timer)
        updatePresetTimers(presetTimers)
    }

    override suspend fun deleteTimer(timer: Timer) {
        timerItems.remove(timer)
        observableTimerItems.postValue(timerItems)
    }

    override suspend fun deletePresetTimer(presetTimer: PresetTimer) {
        presetTimerItems.remove(presetTimer)
        observablePresetTimerItems.postValue(presetTimerItems)
    }

    override suspend fun deletePresetTimers(presetTimers: List<PresetTimer>) {
        presetTimers.forEach { presetTimer ->
            presetTimerItems.remove(presetTimer)
        }
        observablePresetTimerItems.postValue(presetTimerItems)
    }

    override suspend fun deleteTimerAndPresetTimers(timer: Timer, presetTimers: List<PresetTimer>) {
        deleteTimer(timer)
        deletePresetTimers(presetTimers)
    }

    override suspend fun getPresetTimerWithTimer(name: String): TimerWithPresetTimer {
        val timer = timerItems.first { it.name == name }
        val presetTimerList = mutableListOf<PresetTimer>()
        presetTimerItems.forEach { if (it.name == name){ presetTimerList.add(it) }}
        return TimerWithPresetTimer(timer, presetTimerList)
    }

    override suspend fun getCurrentTimer(name: String): Timer {
        return timerItems.first { it.name == name }
    }

    override suspend fun getCurrentPresetTimer(
        timerName: String,
        presetName: String,
        order: Int
    ): PresetTimer {
        return presetTimerItems.first {
            it.name == timerName && it.presetName == presetName && it.timerOrder == order}
    }

    override suspend fun getMaxOrderPresetTimer(name: String): Int {
        val presetTimers = getPresetTimerWithTimer(name)
        var max = 0
        presetTimers.presetTimer.forEach {
            if(it.timerOrder > max) { max = it.timerOrder }
        }
        return max
    }

    override fun observeAllTimer(): LiveData<List<Timer>> {
        return observableTimerItems
    }

    override fun observeAllPresetTimer(): LiveData<List<PresetTimer>> {
        return observablePresetTimerItems
    }

    fun addTimers(vararg timers: Timer) {
        for (timer in timers){
            timerItems.add(timer)
        }
        runBlocking {
            observableTimerItems.value = timerItems
        }
    }
}

