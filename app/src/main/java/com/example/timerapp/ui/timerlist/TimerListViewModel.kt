package com.example.timerapp.ui.timerlist

import android.app.Application
import androidx.lifecycle.*
import com.example.timerapp.database.PresetTimer
import com.example.timerapp.database.Timer
import com.example.timerapp.others.Constants
import com.example.timerapp.others.Event
import com.example.timerapp.others.Resource
import com.example.timerapp.repository.DefaultTimerRepository
import kotlinx.coroutines.launch

class TimerListViewModel(application: Application) : AndroidViewModel(application) {
    private val timerRepository = DefaultTimerRepository.getRepository(application)

    val timerItems = timerRepository.observeAllTimer()

    var timerNames = mutableListOf<String>()

    private val _nameStatus = MutableLiveData<Event<Resource<String>>>()
    val nameStatus: LiveData<Event<Resource<String>>> = _nameStatus

    private val _deleteTimerItemStatus = MutableLiveData<Event<Resource<Timer>>>()
    val deleteTimerItemStatus: LiveData<Event<Resource<Timer>>> = _deleteTimerItemStatus
    private var deletedPresetTimerItems = mutableListOf<PresetTimer>()

    private val _navigateToPresetTimer = MutableLiveData<Event<String>>()
    val navigateToPresetTimer: LiveData<Event<String>> = _navigateToPresetTimer

    private val _navigateToTimer = MutableLiveData<Event<String>>()
    val navigateToTimer: LiveData<Event<String>> = _navigateToTimer

    private val _navigateToDeleteTimer = MutableLiveData<Event<Boolean>>()
    val navigateToDeleteTimer: LiveData<Event<Boolean>> = _navigateToDeleteTimer

    private val _showSnackbarMessage = MutableLiveData<Event<String>>()
    val showSnackbarMessage: LiveData<Event<String>> = _showSnackbarMessage

    private val _showDialog = MutableLiveData<Event<Boolean>>()
    val showDialog: LiveData<Event<Boolean>> = _showDialog

    fun start() {
        viewModelScope.launch {
            timerNames = timerRepository.getTimerNames().toMutableList()
        }
    }

    // insert Timer
    fun createInsertTimerDialog() {
        if(timerNames.count() >= Constants.TIMER_NUM){
            _showSnackbarMessage.value = Event("登録できるタイマーは${Constants.TIMER_NUM}までです。")
        } else {
            _showDialog.value = Event(true)
        }
    }

    fun checkInputTimerName(timerName: String) {
        if (timerName.isEmpty()) {
            _nameStatus.value = Event(Resource.error("タイマー名が入力されていません。", timerName))
            return
        }

        if (timerName.length > Constants.MAX_NAME_LENGTH) {
            _nameStatus.value =
                Event(Resource.error("タイマー名は${Constants.MAX_NAME_LENGTH}文字までです。", timerName))
            return
        }

        if (timerNames.contains(timerName)) {
            _nameStatus.value = Event(Resource.error("入力したタイマー名は使用されています。", timerName))
            return
        }
        _nameStatus.value = Event(Resource.success(timerName))
    }

    fun insertTimer(timerName: String) {
        val timer = Timer(timerName)
        insertTimerIntoDb(timer)
        _navigateToPresetTimer.value = Event(timer.name)
    }

    // testに使用するため　
    private fun insertTimerIntoDb(timer: Timer) {
        viewModelScope.launch {
            timerRepository.insertTimer(timer)
        }
    }

    // swipe delete Timer
    fun deleteTimer(timer: Timer) {
        timerNames.remove(timer.name)
        when (timer.detail) {
            "no presetTimer" -> deleteTimerFromDb(timer)
            else -> deleteTimerAndPresetTimersFromDb(timer)
        }
    }

    private fun deleteTimerFromDb(timer: Timer) {
        viewModelScope.launch {
            timerRepository.deleteTimer(timer)
            _deleteTimerItemStatus.value = Event(Resource.success(timer))
        }
    }

    private fun deleteTimerAndPresetTimersFromDb(timer: Timer) {
        viewModelScope.launch {
            val presetTimers = timerRepository.getPresetTimerWithTimer(timer.name).presetTimer
            timerRepository.deleteTimerAndPresetTimers(timer, presetTimers)
            deletedPresetTimerItems = presetTimers.toMutableList()
            _deleteTimerItemStatus.value = Event(Resource.success(timer))
        }
    }

    // cancel deleteTimer
    fun restoreTimerAndPresetTimers(timer: Timer) {
        timerNames.add(timer.name)
        if (deletedPresetTimerItems.isNotEmpty()) {
            insertTimerAndPresetTimersIntoDb(timer, deletedPresetTimerItems)
        } else {
            insertTimerIntoDb(timer)
        }
    }

    private fun insertTimerAndPresetTimersIntoDb(timer: Timer, presetTimers: List<PresetTimer>) {
        viewModelScope.launch {
            timerRepository.insertTimerAndPresetTimers(timer, presetTimers)
        }
    }

    // Navigation
    fun navigateToPresetTimer(timerName: String) {
        _navigateToPresetTimer.value = Event(timerName)
    }

    fun navigateToTimer(timer: Timer) {
        if (timer.total == 0L) {
            _showSnackbarMessage.value = Event("タイマーが設定されていません。")
            return
        }
        _navigateToTimer.value = Event(timer.name)
    }

    fun navigateToDeleteTimer() {
        if (timerNames.count() == 0) {
            _showSnackbarMessage.value = Event("タイマーが登録されていません。")
        } else {
            _navigateToDeleteTimer.value = Event(true)
        }
    }
}