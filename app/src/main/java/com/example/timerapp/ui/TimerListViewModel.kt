package com.example.timerapp.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timerapp.database.Timer
import com.example.timerapp.others.Constants
import com.example.timerapp.others.Resource
import com.example.timerapp.repository.TimerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

// タイマーを追加する(初期なので名前のみ)
// タイマー削除機能
// PresetTimerFragment,TimerFragmentへの遷移

@HiltViewModel
class TimerListViewModel @Inject constructor(
    private val repository: TimerRepository
): ViewModel(){

    val timerItems = repository.observeAllTimer()

    val timerNamesList = repository.observeAllTimerNames()
    val numberOfTimers = repository.observeNumberOfTimers()

    // Timerをinsertできたかどうか確かめる
    private val _insertTimerItemStatus = MutableLiveData<Resource<Timer>>()
    val insertTimerItemStatus: LiveData<Resource<Timer>> = _insertTimerItemStatus

    // 以下はエラー表示(このfunctionはnameのみの入力)
    // 空の入力
    // 15文字以内
    // 既に同じタイマーの名前が登録されている
    // タイマー数が15個登録されている
    private fun insertTimerIntoDb(timer: Timer) {
        viewModelScope.launch {
            repository.insertTimer(timer)
        }
    }

    fun insertTimer(name: String){
        if(name.isEmpty()){
            _insertTimerItemStatus.postValue(Resource.error(
                "タイマー名が入力されていません。", null
            ))
            return
        }

        if (name.length > Constants.MAX_NAME_LENGTH){
            _insertTimerItemStatus.postValue(Resource.error(
                "タイマー名は${Constants.MAX_NAME_LENGTH}までです。", null
            ))
            return
        }

        val currentTimerNamesList = timerNamesList.value ?: mutableListOf()
        if (currentTimerNamesList.contains(name)){
            _insertTimerItemStatus.postValue(Resource.error(
                    "入力したタイマー名は使用されています。", null
                )
                )
            return
        }

        val currentNumberOfTimers = numberOfTimers.value ?: 0
        if (currentNumberOfTimers >= Constants.TIMER_NUM){
            _insertTimerItemStatus.postValue(Resource.error(
                "登録できるタイマーは${Constants.TIMER_NUM}までです。", null
            )
            )
            return
        }

        val timer = Timer(name)
        insertTimerIntoDb(timer)
        _insertTimerItemStatus.postValue(Resource.success(timer))
    }

    // 関連するPresetTimerも削除する
    fun deleteTimer(timer: Timer){
        viewModelScope.launch {
            repository.deleteTimer(timer)
        }
    }
}