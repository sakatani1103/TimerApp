package com.example.timerapp.ui.deleteTimerList

import android.util.Log
import androidx.lifecycle.*
import com.example.timerapp.database.Timer
import com.example.timerapp.others.Event
import com.example.timerapp.others.Resource
import com.example.timerapp.repository.TimerRepository
import kotlinx.coroutines.launch

class DeleteTimerListViewModel(private val timerRepository: TimerRepository) : ViewModel() {
    val timerItems = timerRepository.observeAllTimer()

    private val _deleteTimerItemStatus = MutableLiveData<Event<Resource<List<Timer>>>>()
    val deleteTimerItemStatus: LiveData<Event<Resource<List<Timer>>>> = _deleteTimerItemStatus

    private val deleteTimerList = mutableListOf<Timer>()

    fun start() {
        deleteTimerList.clear()
    }

    fun deleteTimerListAndPresetTimerList() {
        viewModelScope.launch {
            deleteTimerList.forEach { timer ->
                if (timer.detail == "no presetTimer") {
                    timerRepository.deleteTimer(timer)
                } else {
                    val timerWithPresetTimer = timerRepository.getPresetTimerWithTimer(timer.name)
                    timerRepository.deleteTimerAndPresetTimers(timerWithPresetTimer.timer, timerWithPresetTimer.presetTimer)
                }
            }
            _deleteTimerItemStatus.postValue(Event(Resource.success(deleteTimerList)))
        }
    }

    // update Timer (select Delete Item)
    fun switchTimerIsSelected(timer: Timer) {
        val updateTimer = Timer(
            timer.name, timer.total, timer.listType, timer.notificationType,
            timer.isDisplay, timer.detail, !timer.isSelected, timer.timerId
        )

        if (updateTimer.isSelected) {
            deleteTimerList.add(updateTimer)
        } else {
            deleteTimerList.remove(Timer(timer.name, timer.total, timer.listType, timer.notificationType,
            timer.isDisplay, timer.detail, true, timer.timerId))
            Log.d("ViewModelDelete", "${deleteTimerList}")
        }

        viewModelScope.launch {
            timerRepository.updateTimer(updateTimer)
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
        }
    }

}

@Suppress("UNCHECKED_CAST")
class DeleteTimerListViewModelFactory(
    private val timerRepository: TimerRepository
) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        (DeleteTimerListViewModel(timerRepository) as T)
}