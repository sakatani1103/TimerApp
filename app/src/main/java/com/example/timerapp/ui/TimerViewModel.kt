package com.example.timerapp.ui

import androidx.lifecycle.*
import com.example.timerapp.R
import com.example.timerapp.database.*
import com.example.timerapp.others.*
import com.example.timerapp.repository.TimerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.Result

@HiltViewModel
class TimerViewModel @Inject constructor(
    private val repository: TimerRepository
) : ViewModel() {

    // TimerListFragmentでのタイマー表示、追加に必要
    val timerItems = repository.observeAllTimer()

    // 選択したタイマー関する情報
    private val _currentTimer = MutableLiveData<Timer>()
    val currentTimer: LiveData<Timer> = _currentTimer

    private val _presetTimerList = MutableLiveData<List<PresetTimer>>()
    val presetTimerList: LiveData<List<PresetTimer>> = _presetTimerList

    private val _currentPresetTimer = MutableLiveData<PresetTimer>()
    val currentPresetTimer: LiveData<PresetTimer> = _currentPresetTimer

    private val _timerNamesList = MutableLiveData<List<String>>()
    private val timerNamesList: LiveData<List<String>> = _timerNamesList

    private val _totalTime = MutableLiveData<Int>()
    val totalTime: LiveData<Int> = _totalTime

    private val _temporalPresetTime = MutableLiveData<Int>()
    val temporalPresetTime: LiveData<Int> = _temporalPresetTime

    private val _temporalNotificationTime = MutableLiveData<Int>()
    val temporalNotificationTime: LiveData<Int> = _temporalNotificationTime

    private val _deleteTimerItemStatus = MutableLiveData<Event<Resource<Timer>>>()
    val deleteTimerItemStatus: LiveData<Event<Resource<Timer>>> = _deleteTimerItemStatus

    private val _deletedPresetTimerItems = MutableLiveData<List<PresetTimer>>()
    private val deletedPresetTimerItems: LiveData<List<PresetTimer>> = _deletedPresetTimerItems

    private val _deletePresetTimerStatus = MutableLiveData<Event<Resource<PresetTimer>>>()
    val deletePresetTimerStatus: LiveData<Event<Resource<PresetTimer>>> = _deletePresetTimerStatus

    private val _timerNameStatus = MutableLiveData<Event<Resource<String>>>()
    val timerNameStatus: LiveData<Event<Resource<String>>> = _timerNameStatus

    private val _updateTimerStatus = MutableLiveData<Event<Resource<Timer>>>()
    val updateTimerStatus: LiveData<Event<Resource<Timer>>> = _updateTimerStatus

    private val _insertAndUpdatePresetTimerStatus = MutableLiveData<Event<Resource<PresetTimer>>>()
    val insertAndUpdatePresetTimerStatus: LiveData<Event<Resource<PresetTimer>>> = _insertAndUpdatePresetTimerStatus

    // Navigation
    // TimerListFragmentからPresetTimerListFragmentへの遷移
    private val _navigateToPresetTimer = MutableLiveData<String?>()
    val navigateToPresetTimer: LiveData<String?> = _navigateToPresetTimer

    // TimerListFragmentからTimerFragmentへの遷移
    private val _navigateToTimer = MutableLiveData<String?>()
    val navigateToTimer: LiveData<String?> = _navigateToTimer

    // PresetTimerFragmentからSettingTimerFragmentへの遷移
    private val _navigateToSettingTimer = MutableLiveData<MutableMap<String, String>?>()
    val navigateToSettingTimer: LiveData<MutableMap<String, String>?> = _navigateToSettingTimer

    private val _navigateToDeleteTimer = MutableLiveData<Boolean?>()
    val navigateToDeleteTimer: LiveData<Boolean?> = _navigateToDeleteTimer

    private val _navigateToDeletePresetTimer = MutableLiveData<Boolean?>()
    val navigateToDeletePresetTimer: LiveData<Boolean?> = _navigateToDeletePresetTimer

    private val _showTimerError = MutableLiveData<String?>()
    val showTimerError: LiveData<String?> = _showTimerError

    private val _showTimerDialog = MutableLiveData<Boolean?>()
    val showTimerDialog: LiveData<Boolean?> = _showTimerDialog

    // DB
    private fun insertTimerIntoDb(timer: Timer) {
        viewModelScope.launch {
            repository.insertTimer(timer)
        }
    }

    private fun deleteTimerFromDb(timer: Timer) {
        viewModelScope.launch {
            repository.deleteTimer(timer)
            _deleteTimerItemStatus.postValue(Event(Resource.success(timer)))
        }
    }

    private fun deleteTimerAndRelatedPresetTimers(timer: Timer) {
        viewModelScope.launch {
            val presetTimers = repository.getPresetTimerWithTimer(timer.name).presetTimer
            repository.deleteTimerAndPresetTimers(timer, presetTimers)
            _deletedPresetTimerItems.postValue(presetTimers)
            _deleteTimerItemStatus.postValue(Event(Resource.success(timer)))
        }
    }

    fun restoreTimerAndRelatedPresetTimers(timer: Timer) {
        viewModelScope.launch {
            val presetTimers = deletedPresetTimerItems.value ?: listOf()
            repository.insertTimerAndPresetTimers(timer, presetTimers)
        }
    }

    fun deletePresetTimerAndUpdateRelatedTimer(presetTimer: PresetTimer) {
        viewModelScope.launch {
            repository.deletePresetTimer(presetTimer)
            currentTimer.value?.let { timer ->
                val presetTimers = repository.getPresetTimerWithTimer(timer.name).presetTimer
                // presetTimerが0のときにgetTotalをするとnullになるのでdeleteの時はこの方法
                var total = 0
                presetTimers.forEach { total += it.presetTime }
                val detail = convertDetail(presetTimers)
                val listType = if (presetTimers.count() <= 1) {
                    ListType.SIMPLE_LAYOUT
                } else ListType.DETAIL_LAYOUT
                repository.updateTimer(Timer(
                    timer.name, total, listType, timer.notificationType, timer.isDisplay, detail))
                val timerAndPresetTimers = repository.getPresetTimerWithTimer(timer.name)
                _currentTimer.postValue(timerAndPresetTimers.timer)
                _presetTimerList.postValue(timerAndPresetTimers.presetTimer)
                _deletePresetTimerStatus.postValue(Event(Resource.success(presetTimer)))
            }
        }
    }

    fun restorePresetTimerAndUpdateRelatedTimer(presetTimer: PresetTimer) {
        viewModelScope.launch {
            repository.insertPresetTimer(presetTimer)
            currentTimer.value?.let {
                val presetTimers = repository.getPresetTimerWithTimer(it.name).presetTimer
                val total = repository.getTotalTime(it.name)
                val detail = convertDetail(presetTimers)
                val listType = if (presetTimers.count() <= 1) {
                    ListType.SIMPLE_LAYOUT
                } else ListType.DETAIL_LAYOUT
                repository.updateTimer(
                    Timer(
                    it.name, total, listType, it.notificationType, it.isDisplay, detail))
                val timerAndPresetTimers = repository.getPresetTimerWithTimer(it.name)
                _currentTimer.postValue(timerAndPresetTimers.timer)
                _presetTimerList.postValue(timerAndPresetTimers.presetTimer)
            }
        }
    }

    private fun insertPresetTimerAndUpdateRelatedTimer(presetTimerName: String, presetTime: Int){
        viewModelScope.launch {
            currentTimer.value?.let { timer ->
                val order = if( timer.detail == "no presetTimer"){ 1 }
                else { repository.getMaxOrderPresetTimer(timer.name) + 1 }
                val presetTimer = PresetTimer(timer.name, presetTimerName, order, presetTime, temporalNotificationTime.value!!)
                repository.insertPresetTimer(presetTimer)

                val presetTimers = repository.getPresetTimerWithTimer(timer.name).presetTimer
                val detail = convertDetail(presetTimers)
                val total = repository.getTotalTime(timer.name)
                val listType = if (presetTimers.count() <= 1) {
                    ListType.SIMPLE_LAYOUT
                } else ListType.DETAIL_LAYOUT
                val newTimer = Timer(timer.name, total, listType, timer.notificationType, timer.isDisplay, detail)
                repository.updateTimers(listOf(newTimer))

                val timerAndPresetTimers = repository.getPresetTimerWithTimer(timer.name)
                _currentTimer.postValue(timerAndPresetTimers.timer)
                _presetTimerList.postValue(timerAndPresetTimers.presetTimer)
                _insertAndUpdatePresetTimerStatus.postValue(Event(Resource.success(presetTimer)))
            }
        }
    }

    // Timer名(主キー)以外の変更
    private fun updateTimerIntoDb(timer: Timer) {
        viewModelScope.launch {
            repository.updateTimer(timer)
            _currentTimer.postValue(repository.getCurrentTimer(timer.name))
        }
    }

    private fun updateTimerAndRelatedPresetTimer(newTimer: Timer) {
        viewModelScope.launch {
            currentTimer.value?.let { currentTimer ->
                val presetTimers = repository.getPresetTimerWithTimer(currentTimer.name).presetTimer
                val newPresetTimers = mutableListOf<PresetTimer>()
                presetTimers.forEach{ presetTimer ->
                    newPresetTimers.add(PresetTimer(newTimer.name, presetTimer.presetName, presetTimer.timerOrder,
                        presetTimer.presetTime, presetTimer.notificationTime))
                }
                repository.insertTimerAndPresetTimers(newTimer, newPresetTimers)
                repository.deleteTimerAndPresetTimers(currentTimer, presetTimers)
            }
            val timerAndPresetTimers = repository.getPresetTimerWithTimer(newTimer.name)
            _currentTimer.postValue(timerAndPresetTimers.timer)
            _presetTimerList.postValue(timerAndPresetTimers.presetTimer)
            _updateTimerStatus.postValue(Event(Resource.success(newTimer)))
        }
    }

    private fun deleteAndInsertPresetTimerAndUpdateTimer(presetTimer: PresetTimer) {
        viewModelScope.launch {
            currentPresetTimer.value?.let { repository.deletePresetTimer(it) }
            repository.insertPresetTimer(presetTimer)

            currentTimer.value?.let {
                val presetTimers = repository.getPresetTimerWithTimer(it.name).presetTimer
                val detail = convertDetail(presetTimers)
                val total = repository.getTotalTime(it.name)
                val listType = if (presetTimers.count() <= 1) {
                    ListType.SIMPLE_LAYOUT
                } else ListType.DETAIL_LAYOUT
                repository.updateTimer(
                    Timer(it.name, total, listType, it.notificationType,
                    it.isDisplay,  detail))

                val timerAndPresetTimers = repository.getPresetTimerWithTimer(it.name)
                _currentTimer.postValue(timerAndPresetTimers.timer)
                _presetTimerList.postValue(timerAndPresetTimers.presetTimer)
            }
        }
    }

    private fun updatePresetTimerAndTimer(presetTimer: PresetTimer){
        viewModelScope.launch {
            currentTimer.value?.let {
                val presetTimers = repository.getPresetTimerWithTimer(it.name).presetTimer
                val detail = convertDetail(presetTimers)
                val total = repository.getTotalTime(it.name)
                val listType = if (presetTimers.count() <= 1) {
                    ListType.SIMPLE_LAYOUT
                } else ListType.DETAIL_LAYOUT
                val updateTimer = Timer(it.name, total, listType, it.notificationType,
                    it.isDisplay, detail)
                repository.updateTimerAndPresetTimers(updateTimer, listOf(presetTimer))

                val timerAndPresetTimers = repository.getPresetTimerWithTimer(it.name)
                _currentTimer.postValue(timerAndPresetTimers.timer)
                _presetTimerList.postValue(timerAndPresetTimers.presetTimer)
            }
        }
    }

    private fun deleteTimerListAndPresetTimerList(timerList: List<Timer>) {
        viewModelScope.launch {
            timerList.forEach { timer ->
                if (timer.detail == "no presetTimer") {
                    repository.deleteTimer(timer)
                } else {
                    val presetTimers = repository.getPresetTimerWithTimer(timer.name).presetTimer
                    repository.deleteTimerAndPresetTimers(timer, presetTimers)
                }
            }
            _deleteTimerItemStatus.postValue(Event(Resource.success(null)))
        }
    }


    fun switchTimerIsSelected(timer: Timer){
        viewModelScope.launch {
            repository.updateTimer(
                Timer(timer.name, timer.total, timer.listType, timer.notificationType,
                    timer.isDisplay, timer.detail, !timer.isSelected)
            )
        }
    }

    fun cancelDeleteTimerList(timerList: List<Timer>){
        viewModelScope.launch {
            val updateTimers = mutableListOf<Timer>()
            timerList.forEach {
                updateTimers.add(
                    Timer(it.name, it.total, it.listType, it.notificationType, it.isDisplay, it.detail, false)
                )
            }
            repository.updateTimers(updateTimers)
            _deleteTimerItemStatus.postValue(Event(Resource.success(null)))
        }
    }

    fun switchPresetTimerIsSelected(presetTimer: PresetTimer){
        viewModelScope.launch {
            repository.updatePresetTimers(
                listOf(PresetTimer(presetTimer.name, presetTimer.presetName, presetTimer.timerOrder,
                presetTimer.presetTime, presetTimer.notificationTime, !presetTimer.isSelected))
            )
            _presetTimerList.postValue(repository.getPresetTimerWithTimer(presetTimer.name).presetTimer)
        }
    }

    fun cancelDeletePresetTimerList(presetTimerList: List<PresetTimer>){
        viewModelScope.launch {
            val updatePresetTimers = mutableListOf<PresetTimer>()
            presetTimerList.forEach { presetTimer ->
                updatePresetTimers.add(PresetTimer(presetTimer.name, presetTimer.presetName, presetTimer.timerOrder,
                presetTimer.presetTime, presetTimer.notificationTime, false))
            }
            repository.updatePresetTimers(updatePresetTimers)
        }
        _deletePresetTimerStatus.postValue(Event(Resource.success(null)))
    }

    fun deletePresetTimerList(deletePresetTimers: List<PresetTimer>) {
        viewModelScope.launch {
            currentTimer.value?.let { timer ->
                repository.deletePresetTimers(deletePresetTimers)
                val presetTimers = repository.getPresetTimerWithTimer(timer.name).presetTimer
                _presetTimerList.postValue(presetTimers)

                val detail = convertDetail(presetTimers)
                // presetTimerが無い時にgetTotalTimeをするとnullになる
                var total = 0
                presetTimers.forEach { total += it.presetTime }
                val listType = if (presetTimers.count() <= 1) ListType.SIMPLE_LAYOUT
                else ListType.DETAIL_LAYOUT
                val updateTimer = Timer(timer.name, total, listType, timer.notificationType, timer.isDisplay, detail)
                _currentTimer.postValue(updateTimer)
                repository.updateTimer(updateTimer)

                _deletePresetTimerStatus.postValue(Event(Resource.success(null)))
            }
        }
    }

    fun getCurrentTimerAndPresetTimerList(timerName: String) {
        viewModelScope.launch {
            _currentTimer.postValue(repository.getCurrentTimer(timerName))
            _presetTimerList.postValue(repository.getPresetTimerWithTimer(timerName).presetTimer)
        }
    }
    
    private fun getTimerNamesList() {
        viewModelScope.launch {
            _timerNamesList.postValue(repository.getTimerNames())
        }
    }

    // settingTimerFragmentとTimerFragmentに以降する際に必要
    fun getTemporalTime(timerName: String, presetName: String, order: Int) {
        viewModelScope.launch {
            val presetTimer = repository.getCurrentPresetTimer(timerName,presetName,order)
            _currentPresetTimer.postValue(presetTimer)
            _temporalNotificationTime.postValue(presetTimer.notificationTime)
            _temporalPresetTime.postValue(presetTimer.presetTime)
        }
    }

    // SetTimerFragmentの設定に必要
    fun getTemporalNotificationTime(time: Int) {
        _temporalNotificationTime.postValue(time)
    }

    fun getTemporalPresetTime(time: Int) {
        _temporalPresetTime.postValue(time)
    }

    fun checkInputTimerName(timerName: String) {
        if (timerName.isEmpty()) {
            _timerNameStatus.postValue(
                Event(Resource.error("タイマー名が入力されていません。", timerName))
            )
            return
        }

        if (timerName.length > Constants.MAX_NAME_LENGTH) {
            _timerNameStatus.postValue(
                Event(Resource.error("タイマー名は${Constants.MAX_NAME_LENGTH}文字までです。", timerName))
            )
            return
        }

        getTimerNamesList()
        if ((timerNamesList.value ?: mutableListOf()).contains(timerName)) {
            _timerNameStatus.postValue(
                Event(Resource.error("入力したタイマー名は使用されています。", timerName))
            )
            return
        }

        if ((timerNamesList.value?.count() ?: 0) >= Constants.TIMER_NUM) {
            _timerNameStatus.postValue(
                Event(Resource.error("登録できるタイマーは${Constants.TIMER_NUM}までです。", timerName))
            )
            return
        }
        _timerNameStatus.postValue(Event(Resource.success(timerName)))
    }

    private fun checkInputPresetTimer(presetName: String, presetTime: Int) : Boolean {
        if (presetName.isEmpty()) {
            _timerNameStatus.postValue(
                Event(
                    Resource.error(
                        "タイマー名が入力されていません。", presetName
                    )
                )
            )
            return false
        }

        if (presetName.length > Constants.MAX_NAME_LENGTH) {
            _timerNameStatus.postValue(
                Event(
                    Resource.error(
                        "タイマー名は${Constants.MAX_NAME_LENGTH}文字までです。", presetName
                    )
                )
            )
            return false
        }

        if (presetTime <= (temporalNotificationTime.value ?: 0) || presetTime == 0) {
            _showTimerError.postValue("適切にタイマーの設定を行って下さい。")
            return false
        }

        _timerNameStatus.postValue(Event(Resource.success(null)))
        return true
    }


    fun insertTimer(name: String) {
        val timer = Timer(name)
        insertTimerIntoDb(timer)
        navigateToPresetTimer(name)
    }

    fun deleteTimer(timer: Timer) {
        when (timer.detail) {
            "no presetTimer" -> deleteTimerFromDb(timer)
            else -> deleteTimerAndRelatedPresetTimers(timer)
        }
    }

    fun deleteTimerList(timerList: List<Timer>) {
        if (timerList.count() > 0) {
            deleteTimerListAndPresetTimerList(timerList)
        } else {
            _deleteTimerItemStatus.postValue(Event(Resource.success(null)))
        }
    }

    fun updateSettingTimer(notificationType: NotificationType, isDisplay: Boolean) {
        val timer = currentTimer.value!!
        val newTimer = Timer(
            timer.name, timer.total, timer.listType, notificationType, isDisplay, timer.detail
        )
        updateTimerIntoDb(newTimer)
        _updateTimerStatus.postValue(Event(Resource.success(newTimer)))
    }

    fun updateTimerNameAndSetting(
        timerName: String,
        notificationType: NotificationType,
        isDisplay: Boolean
    ) {
        val timer = currentTimer.value!!
        val newTimer = Timer(
            timerName, timer.total, timer.listType, notificationType, isDisplay, timer.detail
        )
        updateTimerAndRelatedPresetTimer(newTimer)
    }

    fun insertPresetTimer(presetName: String, presetTime: Int) {
        if (checkInputPresetTimer(presetName, presetTime)){
            insertPresetTimerAndUpdateRelatedTimer(presetName, presetTime)
        } else { return }
    }

    // presetName変更の場合は今のPresetTimer,Timerを削除して、presetNameを変更したものを追加
    // presetName以外の項目を追加の場合は更新処理
    fun updatePresetTimer(presetName: String, presetTime: Int) {
        val presetTimer = currentPresetTimer.value!!
        val updatePresetTimer = PresetTimer(presetTimer.name, presetName, presetTimer.timerOrder,
            presetTime, temporalNotificationTime.value!!)
        if (checkInputPresetTimer(presetName, presetTime)){
            if (presetName != presetTimer.presetName) {
                deleteAndInsertPresetTimerAndUpdateTimer(updatePresetTimer)
            } else {
                updatePresetTimerAndTimer(updatePresetTimer)
            }
            _insertAndUpdatePresetTimerStatus.postValue(Event(Resource.success(updatePresetTimer)))
        } else { return }
    }

    // related to Navigation
    // TimerListFragmentからPresetTimerListFragmentへの遷移
    fun navigateToPresetTimer(name: String) {
        getCurrentTimerAndPresetTimerList(name)
        _navigateToPresetTimer.value = name
    }

    // 遷移終了後にnullにする
    fun doneNavigateToPresetTimer() {
        _navigateToPresetTimer.value = null
    }

    fun navigateToSettingTimer(timerName: String?, presetName: String?, order: Int?) {
        val numberOfPresetTimers = presetTimerList.value?.count() ?: 0
        if (numberOfPresetTimers >= Constants.PRESET_TIMER_NUM) {
            _showTimerError.postValue("カスタマイズできるタイマーは${Constants.PRESET_TIMER_NUM}までです。")
            return
        }
        if (timerName != null && presetName != null && order != null){
            getTemporalTime(timerName, presetName, order)
        } else {
            _temporalPresetTime.postValue(0)
            _temporalNotificationTime.postValue(0)
        }

        val nameMap = mutableMapOf(
                    "timerName" to (timerName ?: currentTimer.value!!.name),
                    "presetName" to (presetName ?: "no name"),
                    "order" to (order.toString())
                )

        _navigateToSettingTimer.postValue(nameMap)
    }


    fun doneNavigateToSettingTimer() {
        _navigateToSettingTimer.value = null
    }

    fun navigateToTimer(name: String) {
        getCurrentTimerAndPresetTimerList(name)
        _navigateToTimer.value = name
    }

    fun doneNavigateToTimer() {
        _navigateToTimer.value = null
    }

    // trueならdelete画面へ遷移,　falseならSnackbar表示
    // StatusとEventにした方が良いかも
    fun navigateToDeleteTimer() {
        if (timerItems.value != null) {
            if (timerItems.value!!.count() == 0) {
                _showTimerError.postValue("タイマーが登録されていません。")
            } else {
                _navigateToDeleteTimer.value = true
            }
        }
    }

    fun doneNavigateToDeleteTimer() {
        _navigateToDeleteTimer.value = null
    }

    fun navigateToDeletePresetTimer() {
        if (presetTimerList.value != null) {
            if (presetTimerList.value!!.count() == 0) {
                _showTimerError.postValue("タイマーが登録されていません。")
            } else {
                _navigateToDeletePresetTimer.value = true
            }
        }
    }

    fun doneNavigateToDeletePresetTimer() {
        _navigateToDeletePresetTimer.value = null
    }

    fun createInsertTimerDialog() {
        if ((timerItems.value?.count() ?: 0) >= Constants.TIMER_NUM) {
            _showTimerError.postValue("登録できるタイマーは${Constants.TIMER_NUM}までです。")
        } else {
            _showTimerDialog.postValue(true)
            }
    }

    fun doneShowTimerError() {
        _showTimerError.postValue(null)
    }

    fun doneShowTimerDialog() {
        _showTimerDialog.postValue(null)
    }

}

