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

    //val name = "timer1" // test name

    // TimerListFragmentでのタイマー表示、追加に必要
    val timerItems = repository.observeAllTimer()
    val timerNamesList = repository.observeAllTimerNames()
    val numberOfTimers = repository.observeNumberOfTimers()

    // 選択したタイマー関する情報
    private val _currentTimerName = MutableLiveData<String>()
    val currentTimerName: LiveData<String> = _currentTimerName

    private val _currentTimer = MutableLiveData<Timer>()
    val currentTimer: LiveData<Timer> = _currentTimer

    private val currentTimerWithPresetTimerList = MutableLiveData<TimerWithPresetTimer>()
    private val _presetTimerList = MutableLiveData<List<PresetTimer>>()
    val presetTimerList: LiveData<List<PresetTimer>> = _presetTimerList

    private val _currentPresetTimer = MutableLiveData<PresetTimer>()
    val currentPresetTimer: LiveData<PresetTimer> = _currentPresetTimer

    private val _currentNumberOfPresetTimers = MutableLiveData<Int>()
    val currentNumberOfPresetTimers: LiveData<Int> = _currentNumberOfPresetTimers

    // Timerをinsertできたかどうか確かめる
    private val _insertTimerItemStatus = MutableLiveData<Resource<Timer>>()
    val insertTimerItemStatus: LiveData<Resource<Timer>> = _insertTimerItemStatus

    // PresetTimerをinsertできたかどうか確かめる
    private val _updatePresetTimerItemStatus = MutableLiveData<Resource<Timer>>()
    val updatePresetTimerItemStatus: LiveData<Resource<Timer>> = _updatePresetTimerItemStatus

    // Navigation
    // TimerListFragmentからPresetTimerListFragmentへの遷移
    private val _navigateToPresetTimer = MutableLiveData<String?>()
    val navigateToPresetTimer: LiveData<String?> = _navigateToPresetTimer

    // TimerListFragmentからTimerFragmentへの遷移
    private val _navigateToTimer = MutableLiveData<Boolean?>()
    val navigateToTimer: LiveData<Boolean?> = _navigateToTimer

    // PreseTimerFragmentからSettingTimerFragmentへの遷移
    private val _navigateToSettingTimer = MutableLiveData<Long?>()
    val navigateToSettingTimer: LiveData<Long?> = _navigateToSettingTimer

//    init {
//        getCurrentTimer(name)
//        _presetTimerList.value = getCurrentTimerWithPresetTimerList(name)
//    }

    // DB
    private fun insertTimerIntoDb(timer: Timer) {
        viewModelScope.launch {
            repository.insertTimer(timer)
        }
    }

    // 関連するPresetTimerも削除することに注意
    private fun deleteTimer(timer: Timer){
        viewModelScope.launch {
            repository.deleteTimer(timer)
        }
    }

    // PresetTimerがない場合にSnackbarを表示
    // PresetTimerが1以下になった場合にTimerをSimpleLayoutに変更する処理も記述する
    fun deletePresetTimer(presetTimer: PresetTimer){
        viewModelScope.launch {
            repository.deletePresetTimer(presetTimer)
        }
    }

    // Timer名が変更された時にPresetTimerのTimer名も変更する
    private fun updatePresetTimerFromName(presetTimer: PresetTimer, name: String){
        // nameだけ変更
        val newPresetTimer = PresetTimer(name, presetTimer.presetName, presetTimer.presetTime,
            presetTimer.notificationTime, presetTimer.presetTimerId)
        viewModelScope.launch {
            repository.updatePresetTimer(newPresetTimer)
        }
    }

    // 追加の際はデフォルトで、プリセットタイマー1のようにプリセットタイマー数+1の名前で保存
    // 最初は追加せずにやった方がいいかも？？
    fun addPresetTimer(){
        val presetNum = currentNumberOfPresetTimers.value.toString()
        val newPresetTimerName = "presetTimer$presetNum"
        val presetTimer = PresetTimer("currentTimer.name", newPresetTimerName,
            0,0)
        viewModelScope.launch {
            repository.insertPresetTimer(presetTimer)
        }
    }

    // Timer名(主キー)以外の変更
    private fun updateTimerIntoDb(timer: Timer){
        viewModelScope.launch {
            repository.updateTimer(timer)
        }
    }

    // related to get Data
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

    fun getNumberOfPresetTimers(name: String){
        viewModelScope.launch {
            _currentNumberOfPresetTimers.postValue(repository.getNumberOfPresetTimers(name))
        }
    }

    // related to Insert Timer
    fun insertTimer(name: String){
        if(name.isEmpty()){
            _insertTimerItemStatus.postValue(Resource.error(
                "タイマー名が入力されていません。", null
            ))
            return
        }

        if (name.length > Constants.MAX_NAME_LENGTH){
            _insertTimerItemStatus.postValue(Resource.error(
                "タイマー名は${Constants.MAX_NAME_LENGTH}文字までです。", null
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

    // Timer名変更の処理
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
        val timer = currentTimer.value!! //現在指定してるタイマー
        val newTimer = Timer(name, timer.total, timer.listType, timer.notificationType,
            timer.isDisplay , timer.detail)
        insertTimerIntoDb(newTimer)
        // プリセットタイマーを変更(登録されていれば)
        val presetTimerList = presetTimerList.value
        if (presetTimerList != null && presetTimerList.count() > 0) {
            updatePresetTimerList(presetTimerList, name)
        }
        //　現在のタイマーを削除して、新しいタイマーをcurrentTimerに登録
        deleteTimer(timer)
        getCurrentTimer(name)

        _updatePresetTimerItemStatus.postValue(Resource.success(currentTimer.value))
    }

    private fun updatePresetTimerList(presetTimerList: List<PresetTimer>, name: String){
        presetTimerList.forEach { updatePresetTimerFromName(it, name) }
    }

    // related setting
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

    // related to Navigation
    // TimerListFragmentからPresetTimerListFragmentへの遷移
    fun navigateToPresetTimer(name: String){
        _navigateToPresetTimer.value = name
    }

    // 遷移終了後にnullにする
    fun doneNavigateToPresetTimer() {
        _navigateToPresetTimer.value = null
    }

}