package com.example.timerapp.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timerapp.database.PresetTimer
import com.example.timerapp.database.Timer
import com.example.timerapp.database.TimerWithPresetTimer
import com.example.timerapp.others.Resource
import com.example.timerapp.repository.TimerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SetTimerViewModel @Inject constructor(
    private val repository: TimerRepository
) : ViewModel() {

    val id = 1L // test

    //プリセットタイマーの作成と更新を行う
    //プリセットタイマー変更に伴うタイマーの変更も行う
    //保存ボタンが押されると変更

    private val _currentTimer = MutableLiveData<Timer>()
    val currentTimer: LiveData<Timer> = _currentTimer

    private val _currentPresetTimer = MutableLiveData<PresetTimer>()
    val currentPresetTimer: LiveData<PresetTimer> = _currentPresetTimer

    private val _presetTimerList = MutableLiveData<List<PresetTimer>>()
    val presetTimerList: LiveData<List<PresetTimer>> = _presetTimerList

    private val currentTimerWithPresetTimerList = MutableLiveData<TimerWithPresetTimer>()

    init {
        getCurrentPresetTimer(id)
        getCurrentTimer(_currentPresetTimer.value!!.name)
    }

    fun getCurrentTimer(name: String){
        viewModelScope.launch {
            val timer = repository.getCurrentTimer(name)
            _currentTimer.postValue(timer)
        }
    }

    fun getCurrentPresetTimer(id: Long){
        viewModelScope.launch {
            val presetTimer = repository.getCurrentPresetTimer(id)
            _currentPresetTimer.postValue(presetTimer)
        }
    }

    fun getCurrentTimerWithPresetTimerList(name: String) : List<PresetTimer>{
        viewModelScope.launch {
            currentTimerWithPresetTimerList.value = repository.getPresetTimerWithTimer(name)
        }
        val errorList = listOf<PresetTimer>()
        return currentTimerWithPresetTimerList.value?.presetTimer ?: errorList
    }

}