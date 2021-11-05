package com.example.timerapp.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timerapp.database.*
import com.example.timerapp.others.Resource
import com.example.timerapp.repository.TimerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TimerViewModel @Inject constructor(
    private val repository: TimerRepository
    ): ViewModel() {

    val timerItems = repository.observeAllTimer()

    // バッキングプロパティ
    private val _timerName = MutableLiveData<String>()
    val timerName: LiveData<String> = _timerName

    private val _currentTimer = MutableLiveData<Timer>()
    val currentTimer: LiveData<Timer> = _currentTimer

    private val _presetTimerItems = MutableLiveData<TimerWithPresetTimer>()
    val presetTimerItems: LiveData<TimerWithPresetTimer> = _presetTimerItems

    // Timerをinsertできたかどうか確かめる
    private val _insertTimerItemStatus = MutableLiveData<Resource<Timer>>()
    val insertTimerItemStatus: LiveData<Resource<Timer>> = _insertTimerItemStatus
    // PresetTimerをinsertできたかどうか確かめる
    private val _insertPresetTimerItemStatus = MutableLiveData<Resource<PresetTimer>>()
    val insertPresetTimerItemStatus : LiveData<Resource<PresetTimer>> = _insertPresetTimerItemStatus

    // 以下はエラー表示(このfunctionはnameのみの入力)
    // 空の入力
    // 15文字以内
    // 既に同じタイマーの名前が登録されている
    fun insertTimerIntoDB(timer: Timer) {
        viewModelScope.launch {
            repository.insertTimer(timer)
        }
    }

    fun insertTimer(name: String){
        if(name.isEmpty()){
            _insertTimerItemStatus.postValue(Resource.error(
                "the timer name must not be empty", null
            ))
            return
        }
        val timer = Timer(name)
        insertTimerIntoDB(timer)
        _insertTimerItemStatus.postValue(Resource.success(timer))
    }

    fun insertPresetTimer(name: String, presetName: String, presetTime: Int, notificationTime: Int){
        viewModelScope.launch {
            val presetTimer = PresetTimer(name, presetName, presetTime, notificationTime)
            repository.insertPresetTimer(presetTimer)
        }
    }

    fun updateTimer(name: String, total: Int, listType: ListType, notificationType: NotificationType){
        viewModelScope.launch {
            val timer = Timer(name, total, listType, notificationType)
            repository.updateTimer(timer)
        }
    }

    fun updatePresetTimer(name: String, presetName: String, presetTime: Int, notificationTime: Int) {
        viewModelScope.launch {
            val presetTimer = PresetTimer(name, presetName, presetTime, notificationTime)
            repository.updatePresetTimer(presetTimer)
        }
    }

    fun deleteTimer(timer: Timer){
        viewModelScope.launch {
            repository.deleteTimer(timer)
        }
    }

    fun deletePresetTimer(presetTimer: PresetTimer){
        viewModelScope.launch {
            repository.deletePresetTimer(presetTimer)
        }
    }

    // 選択したタイマーに対応するプリセットタイマーを表示、またプリセットタイマーの詳細を取得
    fun getPresetTimerWithTimer(name: String){
        viewModelScope.launch {
            _presetTimerItems.value = repository.getPresetTimerWithTimer(name)
        }
    }

    fun getCurrentTimer(name: String) {
        viewModelScope.launch {
            _currentTimer.value = repository.getCurrentTimer(name)
        }
    }

    // タイマーが削除された時にプリセットタイマーも削除する
    fun deleteTimerWithPresetTimer(timer: Timer){
        val timerName = timer.name
        viewModelScope.launch {
            val timerWithPresetTimer = repository.getPresetTimerWithTimer(timerName)
            val presetTimers = timerWithPresetTimer.presetTimer
            presetTimers.map {
                repository.deletePresetTimer(it)
            }
        }
        deleteTimer(timer)
    }
}