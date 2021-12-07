package com.example.timerapp.ui.presetTimerList

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.timerapp.database.ListType
import com.example.timerapp.database.NotificationType
import com.example.timerapp.database.PresetTimer
import com.example.timerapp.database.Timer
import com.example.timerapp.others.Constants
import com.example.timerapp.others.Event
import com.example.timerapp.others.Resource
import com.example.timerapp.others.convertDetail
import com.example.timerapp.repository.DefaultTimerRepository
import kotlinx.coroutines.launch

class PresetTimerListViewModel(application: Application) : AndroidViewModel(application) {
    private val timerRepository = DefaultTimerRepository.getRepository(application)

    var timerNames = mutableListOf<String>()

    private val _updateTimerStatus = MutableLiveData<Event<Resource<Timer>>>()
    val updateTimerStatus: LiveData<Event<Resource<Timer>>> = _updateTimerStatus

    private val _deletePresetTimerStatus = MutableLiveData<Event<Resource<PresetTimer>>>()
    val deletePresetTimerStatus: LiveData<Event<Resource<PresetTimer>>> = _deletePresetTimerStatus

    private val _navigateToTimerList = MutableLiveData<Event<Boolean>>()
    val navigateToTimerList: LiveData<Event<Boolean>> = _navigateToTimerList

    private val _navigateToTimer = MutableLiveData<Event<String>>()
    val navigateToTimer: LiveData<Event<String>> = _navigateToTimer

    private val _navigateToSettingTimer = MutableLiveData<Event<Map<String, String?>>>()
    val navigateToSettingTimer: LiveData<Event<Map<String, String?>>> = _navigateToSettingTimer

    private val _navigateToDeletePresetTimer = MutableLiveData<Event<String>>()
    val navigateToDeletePresetTimer: LiveData<Event<String>> = _navigateToDeletePresetTimer

    private val _showSnackbarMessage = MutableLiveData<Event<String>>()
    val showSnackbarMessage: LiveData<Event<String>> = _showSnackbarMessage

    private var currentTimer = MutableLiveData<Timer>()
    var currentTimerName = MutableLiveData<String>()
    var selectedPosition = MutableLiveData<Int>()
    var currentTimerSound = MutableLiveData(NotificationType.VIBRATION)
    var isInitial = MutableLiveData(true)
    private val _presetTimerList = MutableLiveData<List<PresetTimer>>()
    val presetTimerList: LiveData<List<PresetTimer>> = _presetTimerList
    val soundTypes = MutableLiveData(arrayOf("バイブレーション", "アラーム"))

    fun start(timerName: String) {
        viewModelScope.launch {
            val timerWithPresetTimer = timerRepository.getPresetTimerWithTimer(timerName)
            currentTimer.value = timerWithPresetTimer.timer
            currentTimerName.value = timerWithPresetTimer.timer.name
            currentTimerSound.value = timerWithPresetTimer.timer.notificationType
            isInitial.value = timerWithPresetTimer.timer.total == 0L
            if (!isInitial.value!!) {
                _presetTimerList.value =
                    sortedOrder(timerWithPresetTimer.presetTimer)
            }
            timerNames = timerRepository.getTimerNames().toMutableList()
        }
    }

    private fun sortedOrder(presetTimerList: List<PresetTimer>): List<PresetTimer> {
        val comparator: Comparator<PresetTimer> = compareBy { it.timerOrder }
        return presetTimerList.sortedWith(comparator)
    }

    fun onItemSelectedNumber(position: Int) {
        currentTimerSound.value = if (soundTypes.value?.get(position) == "バイブレーション") {
            selectedPosition.value = 0
            NotificationType.VIBRATION
        } else {
            selectedPosition.value = 1
            NotificationType.ALARM
        }
    }

    fun updateSettingTimer() {
        currentTimer.value?.let { timer ->
            currentTimerName.value?.let { timerName ->
                val updateTimer = Timer(
                    timerName, timer.total, timer.listType, currentTimerSound.value!!,
                    timer.isDisplay, timer.detail, false, timer.timerId
                )

                if (timerName.isEmpty()) {
                    _updateTimerStatus.value =
                        Event(Resource.error("タイマー名が入力されていません。", updateTimer))
                    return
                }

                if (timerName.length > Constants.MAX_NAME_LENGTH) {
                    _updateTimerStatus.value = Event(
                        Resource.error(
                            "タイマー名は${Constants.MAX_NAME_LENGTH}文字までです。",
                            updateTimer
                        )
                    )
                    return
                }

                if (timerNames.contains(timerName) && (timer.name != timerName)) {
                    _updateTimerStatus.value =
                        Event(Resource.error("入力したタイマー名は使用されています。", updateTimer))
                    return
                }
                if (timer.name != timerName) {
                    val presetTimers = presetTimerList.value ?: listOf()
                    val updatePresetTimers = mutableListOf<PresetTimer>()
                    presetTimers.forEach { preset ->
                        updatePresetTimers.add(PresetTimer(
                            timerName, preset.presetName, preset.timerOrder, preset.presetTime,
                        preset.notificationTime, false, preset.presetTimerId))
                    }
                    updateTimerAndPresetTimersIntoDb(updateTimer, updatePresetTimers)
                } else {
                    updateTimerIntoDb(updateTimer)
                }
                _updateTimerStatus.postValue(Event(Resource.success(timer)))
            }
        }
    }

    private fun updateTimerAndPresetTimersIntoDb(timer: Timer, presetTimerList: List<PresetTimer>) {
        viewModelScope.launch {
            timerRepository.updateTimerAndPresetTimers(timer, presetTimerList)
        }
    }

    private fun updateTimerIntoDb(timer: Timer) {
        viewModelScope.launch {
            timerRepository.updateTimer(timer)
        }
    }

    // drag and drop updatePresetTimer
    fun changePresetTimerOrder(fromPos: Int, toPos: Int) {
        val updateOrderList = presetTimerList.value!!.toMutableList()
        if (fromPos < toPos) {
            val fromValue = updateOrderList[fromPos]
            updateOrderList.removeAt(fromPos)
            updateOrderList.add(toPos, fromValue)
        } else {
            val toValue = updateOrderList[toPos]
            updateOrderList.removeAt(toPos)
            updateOrderList.add(fromPos, toValue)
        }
        val modifiedOrderList = mutableListOf<PresetTimer>()
        for ((index, value) in updateOrderList.withIndex()){
            modifiedOrderList.add(PresetTimer(value.name, value.presetName, index+1, value.presetTime,
            value.notificationTime, false, value.presetTimerId)
            )
        }
        deleteAndInsertPresetTimers(modifiedOrderList)
    }

    private fun deleteAndInsertPresetTimers(updateOrderList: List<PresetTimer>) {
        viewModelScope.launch {
            timerRepository.deletePresetTimers(presetTimerList.value!!)
            timerRepository.insertPresetTimers(updateOrderList)
            val updateTimer = timerChangeDueToPresetTimerChange(updateOrderList)
            timerRepository.updateTimer(updateTimer)
            _presetTimerList.value =
                sortedOrder(timerRepository.getPresetTimerWithTimer(updateTimer.name).presetTimer)
        }
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

    // swipe delete PresetTimer
    fun deletePresetTimerAndUpdateTimer(presetTimer: PresetTimer) {
        viewModelScope.launch {
            timerRepository.deletePresetTimer(presetTimer)
            currentTimer.value?.let { timer ->
                val presetTimerList =
                    sortedOrder(timerRepository.getPresetTimerWithTimer(timer.name).presetTimer)
                val updateTimer = timerChangeDueToPresetTimerChange(presetTimerList)
                timerRepository.updateTimer(updateTimer)
                _presetTimerList.value =
                    timerRepository.getPresetTimerWithTimer(timer.name).presetTimer
                _deletePresetTimerStatus.value = Event(Resource.success(presetTimer))
            }
        }
    }

    fun restorePresetTimerAndUpdateTimer(presetTimer: PresetTimer) {
        viewModelScope.launch {
            timerRepository.insertPresetTimer(presetTimer)
            currentTimer.value?.let {
                val presetTimerList =
                    sortedOrder(timerRepository.getPresetTimerWithTimer(it.name).presetTimer)
                val updateTimer = timerChangeDueToPresetTimerChange(presetTimerList)
                timerRepository.updateTimer(updateTimer)
                _presetTimerList.value =
                    sortedOrder(timerRepository.getPresetTimerWithTimer(it.name).presetTimer)
            }
        }
    }

    // Navigation
    fun navigateToSettingTimer(presetTimerId: String?) {
        val numberOfPresetTimers = presetTimerList.value?.count() ?: 0
        if (numberOfPresetTimers >= Constants.PRESET_TIMER_NUM) {
            _showSnackbarMessage.value = Event("カスタマイズできるタイマーは${Constants.PRESET_TIMER_NUM}までです。")
            return
        }
        _navigateToSettingTimer.value = Event(
            mapOf(
                "timerName" to currentTimer.value!!.name,
                "presetTimerId" to presetTimerId
            )
        )
    }

    fun navigateToDeletePresetTimer() {
        val numberOfPresetTimers = presetTimerList.value?.count() ?: 0
        if (numberOfPresetTimers == 0) {
            _showSnackbarMessage.value = Event("タイマーが登録されていません。")
        } else {
            _navigateToDeletePresetTimer.value = Event(currentTimer.value!!.name)
        }
    }

    fun navigateToTimerList() {
        _navigateToTimerList.value = Event(true)
    }

    fun navigateToTimer() {
        val numberOfPresetTimers = presetTimerList.value?.count() ?: 0
        if (numberOfPresetTimers == 0) {
            _showSnackbarMessage.value = Event("タイマーが登録されていません。")
        } else {
            _navigateToTimer.value = Event(currentTimer.value!!.name)
        }
    }
}