package com.example.timerapp.ui.deleteTimerList

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.timerapp.database.Timer
import com.example.timerapp.others.Event
import com.example.timerapp.others.Resource
import com.example.timerapp.repository.DefaultTimerRepository
import kotlinx.coroutines.launch

class DeleteTimerListViewModel(application: Application) : AndroidViewModel(application) {
    private val timerRepository = DefaultTimerRepository.getRepository(application)
    val timerItems = timerRepository.observeAllTimer()

    private val _deleteTimerItemStatus = MutableLiveData<Event<Resource<List<Timer>>>>()
    val deleteTimerItemStatus: LiveData<Event<Resource<List<Timer>>>> = _deleteTimerItemStatus

    private var currentTimer = MutableLiveData<Timer>()
    private var deleteTimerList = mutableListOf<Timer>()

    fun deleteTimerListAndPresetTimerList() {
        viewModelScope.launch {
            deleteTimerList.forEach { timer ->
//                if (timer.detail == "no presetTimer") {
//                    timerRepository.deleteTimer(timer)
//                } else {
//                    val presetTimers =
//                        timerRepository.getPresetTimerWithTimer(timer.name).presetTimer
//                    timerRepository.deleteTimerAndPresetTimers(timer, presetTimers)
//                }
                val timerWithPresetTimer = timerRepository.getPresetTimerWithTimer(timer.name)
                val deleteTimer = timerWithPresetTimer.timer
                val deletePresetTimer = timerWithPresetTimer.presetTimer
                timerRepository.deleteTimerAndPresetTimers(deleteTimer, deletePresetTimer)
            }
            _deleteTimerItemStatus.postValue(Event(Resource.success(deleteTimerList)))
            deleteTimerList.clear()
        }
    }

    // update Timer (select Delete Item)
    fun switchTimerIsSelected(timer: Timer) {
        if (!timer.isSelected) {
            deleteTimerList.add(timer)
        } else {
            deleteTimerList.remove(timer)
        }

        viewModelScope.launch {
            timerRepository.updateTimer(
                Timer(
                    timer.name, timer.total, timer.listType, timer.notificationType,
                    timer.isDisplay, timer.detail, !timer.isSelected, timer.timerId
                )
            )
        }
    }

    fun cancelDeleteTimerList() {
        viewModelScope.launch {
            val updateTimers = mutableListOf<Timer>()
            deleteTimerList.forEach { timer ->
                updateTimers.add(
                    Timer(
                        timer.name,
                        timer.total,
                        timer.listType,
                        timer.notificationType,
                        timer.isDisplay,
                        timer.detail,
                        false,
                        timer.timerId
                    )
                )
            }
            timerRepository.updateTimers(updateTimers)
            _deleteTimerItemStatus.postValue(Event(Resource.success(deleteTimerList)))
            deleteTimerList.clear()
        }
    }

}