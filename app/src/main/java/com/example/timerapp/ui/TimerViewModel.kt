package com.example.timerapp.ui

import androidx.lifecycle.*
import com.example.timerapp.database.*
import com.example.timerapp.others.Constants
import com.example.timerapp.others.Resource
import com.example.timerapp.repository.TimerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TimerViewModel @Inject constructor(
    private val repository: TimerRepository
    ): ViewModel() {
    val name = "timer1" // test

    private val _currentTimer = MutableLiveData<Timer>()
    val currentTimer: LiveData<Timer> = _currentTimer

    private val _presetTimerList = MutableLiveData<List<PresetTimer>>()
    val presetTimerList: LiveData<List<PresetTimer>> = _presetTimerList

    private val currentTimerWithPresetTimerList = MutableLiveData<TimerWithPresetTimer>()

    init {
        getCurrentTimer(name)
        _presetTimerList.value = getCurrentTimerWithPresetTimerList(name)
    }

    fun getCurrentTimer(name: String){
        viewModelScope.launch {
            val timer = repository.getCurrentTimer(name)
            _currentTimer.postValue(timer)
        }
    }

    private fun getCurrentTimerWithPresetTimerList(name: String) : List<PresetTimer>{
        viewModelScope.launch {
            currentTimerWithPresetTimerList.value = repository.getPresetTimerWithTimer(name)
        }
        val errorList = listOf<PresetTimer>()
        return currentTimerWithPresetTimerList.value?.presetTimer ?: errorList
    }

}