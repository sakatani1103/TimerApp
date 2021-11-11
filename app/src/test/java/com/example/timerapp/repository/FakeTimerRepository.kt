package com.example.timerapp.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.timerapp.database.PresetTimer
import com.example.timerapp.database.Timer
import com.example.timerapp.database.TimerDao
import com.example.timerapp.database.TimerWithPresetTimer
import kotlinx.coroutines.runBlocking
import javax.inject.Inject


class FakeTimerRepository: TimerRepository {
    private val timerItems = mutableListOf<Timer>()
    private val observableTimerItems = MutableLiveData<List<Timer>>()

    private val presetTimerItems = mutableListOf<PresetTimer>()
    private val observablePresetTimerItems = MutableLiveData<List<PresetTimer>>()

    private val currentTimerNamesList = mutableListOf<String>()
    private val observableTimerNamesList = MutableLiveData<List<String>>()

    private var currentNumberOfTimers: Int = 0
    private val observableNumberOfTimers = MutableLiveData<Int>()

    val currentTimer = MutableLiveData<Timer>()

    // mutableLiveData.postValueは毎回更新されるので作成したリストを加える
    override suspend fun insertTimer(timer: Timer) {
        timerItems.add(timer)
        observableTimerItems.postValue(timerItems)
        currentTimerNamesList.add(timer.name)
        observableTimerNamesList.postValue(currentTimerNamesList)
        currentNumberOfTimers += 1
        observableNumberOfTimers.postValue(currentNumberOfTimers)
    }

    override suspend fun insertPresetTimer(presetTimer: PresetTimer) {
        presetTimerItems.add(presetTimer)
        observablePresetTimerItems.postValue(presetTimerItems)
    }

    override suspend fun updateTimer(timer: Timer) {
        val timerIndex = timerItems.indexOf(timer)
        timerItems[timerIndex] = timer
        observableTimerItems.postValue(timerItems)
        currentTimerNamesList[timerIndex] = timer.name
        observableTimerNamesList.postValue(currentTimerNamesList)
    }

    override suspend fun updatePresetTimer(presetTimer: PresetTimer) {
        val presetTimerIndex = presetTimerItems.indexOf(presetTimer)
        presetTimerItems[presetTimerIndex] = presetTimer
        observablePresetTimerItems.postValue(presetTimerItems)
    }

    override suspend fun deleteTimer(timer: Timer) {
        timerItems.remove(timer)
        observableTimerItems.postValue(timerItems)
        currentTimerNamesList.remove(timer.name)
        observableTimerNamesList.postValue(currentTimerNamesList)
        currentNumberOfTimers -= 1
        observableNumberOfTimers.postValue(currentNumberOfTimers)
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

    override suspend fun getCurrentTimer(name: String) : Timer{
        val timer = timerItems.first { it.name == name }
        currentTimer.postValue(timer)
        return timer
    }

    override suspend fun getCurrentPresetTimer(id: Long): PresetTimer {
        return presetTimerItems.first{it.presetTimerId == id}
    }

    override suspend fun getNumberOfPresetTimers(name: String): Int {
        return presetTimerItems.count{it.name == name}
    }

    override fun observeAllTimer(): LiveData<List<Timer>> {
        return observableTimerItems
    }

    override fun observeAllPresetTimer(): LiveData<List<PresetTimer>> {
        return observablePresetTimerItems
    }

    override fun observeNumberOfTimers(): LiveData<Int> {
        return observableNumberOfTimers
    }

    override fun observeAllTimerNames(): LiveData<List<String>> {
        return observableTimerNamesList
    }

    override fun observeCurrentTimer(name: String): LiveData<Timer> {
        TODO("Not yet implemented")
    }

    fun addTasks(vararg timers: Timer) {
        for (timer in timers){
            timerItems.add(timer)
            currentTimerNamesList.add(timer.name)
            currentNumberOfTimers += 1
        }
        runBlocking {
            observableTimerItems.value = timerItems
            observableTimerNamesList.value = currentTimerNamesList
            observableNumberOfTimers.value = currentNumberOfTimers
        }
    }
}

