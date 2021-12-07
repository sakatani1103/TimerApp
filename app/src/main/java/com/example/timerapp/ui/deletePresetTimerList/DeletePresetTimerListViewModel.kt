package com.example.timerapp.ui.deletePresetTimerList

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.timerapp.database.ListType
import com.example.timerapp.database.PresetTimer
import com.example.timerapp.database.Timer
import com.example.timerapp.others.Event
import com.example.timerapp.others.Resource
import com.example.timerapp.others.convertDetail
import com.example.timerapp.repository.DefaultTimerRepository
import kotlinx.coroutines.launch

class DeletePresetTimerListViewModel(application: Application) : AndroidViewModel(application) {
    private val timerRepository = DefaultTimerRepository.getRepository(application)

    private val _deletePresetTimerItemStatus = MutableLiveData<Event<Resource<List<PresetTimer>>>>()
    val deletePresetTimerItemStatus: LiveData<Event<Resource<List<PresetTimer>>>> =
        _deletePresetTimerItemStatus

    private val _navigateToPresetTimerList = MutableLiveData<Event<String>>()
    val navigateToPresetTimerList : LiveData<Event<String>> = _navigateToPresetTimerList

    private var deletePresetTimerList = mutableListOf<PresetTimer>()
    private var residuePresetTimerList = mutableListOf<PresetTimer>()
    val currentPresetTimerList = MutableLiveData<List<PresetTimer>>()
    private val currentTimer = MutableLiveData<Timer>()

    fun start(timerName: String) {
        viewModelScope.launch {
            val timerWithPresetTimer = timerRepository.getPresetTimerWithTimer(timerName)
            currentPresetTimerList.value = sortedOrder(timerWithPresetTimer.presetTimer)
            currentTimer.value = timerWithPresetTimer.timer
            residuePresetTimerList = timerWithPresetTimer.presetTimer.toMutableList()
        }
    }

    // deletePresetTimerListFragment
    fun deletePresetTimerList() {
        viewModelScope.launch {
            if (deletePresetTimerList.isNotEmpty()) {
                timerRepository.deletePresetTimers(deletePresetTimerList)
                deletePresetTimerList.forEach { preset ->
                    residuePresetTimerList.remove(preset)
                }
                val updateTimer = timerChangeDueToPresetTimerChange(residuePresetTimerList)
                timerRepository.updateTimer(updateTimer)
                residuePresetTimerList.clear()
            }
            _deletePresetTimerItemStatus.postValue(Event(Resource.success(deletePresetTimerList)))
        }
    }

    private fun sortedOrder(presetTimerList: List<PresetTimer>): List<PresetTimer> {
        val comparator: Comparator<PresetTimer> = compareBy { it.timerOrder }
        return presetTimerList.sortedWith(comparator)
    }

    private fun timerChangeDueToPresetTimerChange(presetTimerList: List<PresetTimer>): Timer {
        var timer = Timer("no name")
        var total = 0L
        presetTimerList.forEach { total += it.presetTime }
        val detail = convertDetail(sortedOrder(presetTimerList))
        val listType = if (presetTimerList.count() <= 1) {
            ListType.SIMPLE_LAYOUT
        } else ListType.DETAIL_LAYOUT
        currentTimer.value?.let {
            timer = Timer(it.name, total, listType, it.notificationType, it.isDisplay, detail, false, it.timerId)
        }
        return timer
    }

    // update presetTimer (select Delete Item)
    fun switchPresetTimerIsSelected(presetTimer: PresetTimer) {
        if (!presetTimer.isSelected) {
            deletePresetTimerList.add(presetTimer)
        } else {
            deletePresetTimerList.remove(presetTimer)
        }
        viewModelScope.launch {
            timerRepository.updatePresetTimer(
                    PresetTimer(
                        presetTimer.name,
                        presetTimer.presetName,
                        presetTimer.timerOrder,
                        presetTimer.presetTime,
                        presetTimer.notificationTime,
                        !presetTimer.isSelected,
                        presetTimer.presetTimerId
                    )
            )
            currentPresetTimerList.value =
                sortedOrder(timerRepository.getPresetTimerWithTimer(presetTimer.name).presetTimer)
        }
    }

    fun cancelDeletePresetTimerList() {
        viewModelScope.launch {
            val updatePresetTimers = mutableListOf<PresetTimer>()
            deletePresetTimerList.forEach { presetTimer ->
                updatePresetTimers.add(
                    PresetTimer(
                        presetTimer.name, presetTimer.presetName, presetTimer.timerOrder,
                        presetTimer.presetTime, presetTimer.notificationTime, false, presetTimer.presetTimerId
                    )
                )
            }
            timerRepository.updatePresetTimers(updatePresetTimers)
        }
        _deletePresetTimerItemStatus.value = Event(Resource.success(deletePresetTimerList))
        deletePresetTimerList.clear()
    }

    fun navigateToPresetTimerList() {
        _navigateToPresetTimerList.value = Event(currentTimer.value!!.name)
    }
}