package com.example.timerapp.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timerapp.database.*
import com.example.timerapp.others.Constants
import com.example.timerapp.others.Resource
import com.example.timerapp.repository.TimerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

// TimerListFragmentでクリックしたPresetTimerを表示
// タイマーの設定はデフォルトで
@HiltViewModel
class PresetTimerListViewModel @Inject constructor(
    private val repository: TimerRepository
): ViewModel() {

    val timerNamesList = repository.observeAllTimerNames()

    val name = "timer1" // 仮のtimer名

    private val _currentTimer = MutableLiveData<Timer>()
    val currentTimer: LiveData<Timer> = _currentTimer

    private val _presetTimerList = MutableLiveData<List<PresetTimer>>()
    val presetTimerList: LiveData<List<PresetTimer>> = _presetTimerList

    private val _currentNumberOfPresetTimers = MutableLiveData<Int>()
    val currentNumberOfPresetTimers: LiveData<Int> = _currentNumberOfPresetTimers

    private val _updatePresetTimerItemStatus = MutableLiveData<Resource<Timer>>()
    val updatePresetTimerItemStatus: LiveData<Resource<Timer>> = _updatePresetTimerItemStatus

    private val currentTimerWithPresetTimerList = MutableLiveData<TimerWithPresetTimer>()

    init {
        getCurrentTimer(name)
        _presetTimerList.value = getCurrentTimerWithPresetTimerList(name)
        getNumberOfPresetTimers(name)
    }

    // タイマー名の変更 下記の場合にERR
    // 空の入力
    // 15文字以内
    // 既に同じタイマーの名前が登録されている(変更なしの場合はスルー)
    fun updateTimerName(name: String){
        if (name.isEmpty()){
            _updatePresetTimerItemStatus.postValue(Resource.error(
                "タイマー名が入力されていません。", null
            ))
            return
        }

        if (name.length > Constants.MAX_NAME_LENGTH){
            _updatePresetTimerItemStatus.postValue(Resource.error(
                "タイマー名は${Constants.MAX_NAME_LENGTH}までです。",null
            ))
            return
        }

        val currentTimerNameList = timerNamesList.value ?: mutableListOf()
        if(currentTimer.value != null) {
            if (currentTimerNameList.contains(name) && currentTimer.value?.name != name) {
                _updatePresetTimerItemStatus.postValue(Resource.error(
                    "入力したタイマー名は使用されています。", null
                )
                )
                return
            }
        }

        updateTimerNameIntoDb(name)
    }

    // タイマー名を変更する場合は、主キーを変更することになるので、
    // タイマーを追加、プリセットタイマー名を変更して変更前のタイマーを削除する
    fun updateTimerNameIntoDb(name: String){
        // 新しいタイマーを追加
        insertTimer(name)
        // プリセットタイマーを変更(登録されていれば)
        val presetTimerList = presetTimerList.value
        if (presetTimerList != null && presetTimerList.count() > 0) {
            updatePresetTimerList(presetTimerList, name)
        }
        //　現在のタイマーを削除して、新しいタイマーをcurrentTimerに登録
        val timer = currentTimer.value!!
        deleteTimer(timer)
        getCurrentTimer(name)
        _updatePresetTimerItemStatus.postValue(Resource.success(currentTimer.value))
    }

    fun switchTimerDisplay(notificationType: NotificationType){
        val timer = currentTimer.value!!
        val newTimer = Timer(timer.name, timer.total, timer.listType, notificationType,
            timer.isDisplay, timer.detail)
        updateTimerIntoDb(newTimer)
    }

    fun settingSound(isDisplay: Boolean){
        val timer = currentTimer.value!!
        val newTimer = Timer(timer.name, timer.total, timer.listType, timer.notificationType
            , isDisplay, timer.detail)
        updateTimerIntoDb(newTimer)
    }


    fun updateTimerIntoDb(timer: Timer){
        viewModelScope.launch {
            repository.updateTimer(timer)
        }
    }

    // ViewModel内でしか使用しない(PresetTimerFragment)
    private fun deleteTimer(timer: Timer){
        viewModelScope.launch {
            repository.deleteTimer(timer)
        }
    }

    // PresetTimerがない場合にSnackbarを表示
    // PresetTimerが1以下になった場合にTimerをSimpleLayoutに変更する
    fun deletePresetTimer(presetTimer: PresetTimer){
        viewModelScope.launch {
            repository.deletePresetTimer(presetTimer)
        }
    }

    fun insertTimer(name: String){
        viewModelScope.launch {
            val timer = currentTimer.value!!
            // nameだけ変更したい
            val newTimer = Timer(name, timer.total, timer.listType, timer.notificationType,
                timer.isDisplay , timer.detail)
            repository.insertTimer(newTimer)
        }
    }

    private fun updatePresetTimerFromName(presetTimer: PresetTimer, name: String){
        // nameだけ変更
        val newPresetTimer = PresetTimer(name, presetTimer.presetName, presetTimer.presetTime,
            presetTimer.notificationTime, presetTimer.presetTimerId)
        viewModelScope.launch {
            repository.updatePresetTimer(newPresetTimer)
        }
    }

    private fun updatePresetTimerList(presetTimerList: List<PresetTimer>, name: String){
        presetTimerList.forEach { updatePresetTimerFromName(it, name) }
    }

    fun getCurrentTimerWithPresetTimerList(name: String) : List<PresetTimer>{
        viewModelScope.launch {
            currentTimerWithPresetTimerList.value = repository.getPresetTimerWithTimer(name)
        }

        if (currentTimerWithPresetTimerList.value == null){
            _updatePresetTimerItemStatus.postValue(Resource.error(
                "PresetTimerがありません。", null
            ))
        }

        val errorList = listOf<PresetTimer>()

        return currentTimerWithPresetTimerList.value?.presetTimer ?: errorList
    }

    fun getCurrentTimer(name: String) {
        viewModelScope.launch {
            val timer = repository.getCurrentTimer(name)
            _currentTimer.postValue(timer)
        }
    }

    fun getNumberOfPresetTimers(name: String){
        viewModelScope.launch {
            _currentNumberOfPresetTimers.value = repository.getNumberOfPresetTimers(name)
        }
    }

    // 追加の際はデフォルトで、プリセットタイマー1のようにプリセットタイマー数+1の名前で保存
    fun addPresetTimer(){
        val presetNum = currentNumberOfPresetTimers.value.toString()
        val newPresetTimerName = "presetTimer$presetNum"
        val presetTimer = PresetTimer("currentTimer.name", newPresetTimerName,
            0,0)
        viewModelScope.launch {
            repository.insertPresetTimer(presetTimer)
        }
    }


}