package com.example.timerapp.ui.timerlist

import androidx.lifecycle.*
import com.example.timerapp.database.PresetTimer
import com.example.timerapp.database.Timer
import com.example.timerapp.others.Constants
import com.example.timerapp.others.Event
import com.example.timerapp.others.Resource
import com.example.timerapp.repository.TimerRepository
import kotlinx.coroutines.launch

class TimerListViewModel(private val timerRepository: TimerRepository) : ViewModel() {

    val timerItems = timerRepository.observeAllTimer()
    val presetTimers = timerRepository.observeAllPresetTimer()

    var timerNames = mutableListOf<String>()

    var isInitial = MutableLiveData<Boolean>()

    private val _nameStatus = MutableLiveData<Event<Resource<String>>>()
    val nameStatus: LiveData<Event<Resource<String>>> = _nameStatus

    private val _deleteTimerItemStatus = MutableLiveData<Event<Resource<Timer>>>()
    val deleteTimerItemStatus: LiveData<Event<Resource<Timer>>> = _deleteTimerItemStatus
    private var deletedPresetTimerItems = mutableListOf<PresetTimer>()

    private val _navigateToPresetTimer = MutableLiveData<Event<String>>()
    val navigateToPresetTimer: LiveData<Event<String>> = _navigateToPresetTimer

    private val _navigateToTimer = MutableLiveData<Event<Resource<String>>>()
    val navigateToTimer: LiveData<Event<Resource<String>>> = _navigateToTimer

    private val _navigateToDeleteTimer = MutableLiveData<Event<Resource<Boolean>>>()
    val navigateToDeleteTimer: LiveData<Event<Resource<Boolean>>> = _navigateToDeleteTimer

    private val _showDialog = MutableLiveData<Event<Resource<String>>>()
    val showDialog: LiveData<Event<Resource<String>>> = _showDialog

    fun start() {
        viewModelScope.launch {
            timerNames = timerRepository.getTimerNames().toMutableList()
            isInitial.value = timerNames.isEmpty()
        }
    }

    // insert Timer
    fun createInsertTimerDialog() {
        if(timerNames.count() >= Constants.TIMER_NUM){
            _showDialog.value = Event(Resource.error("??????????????????????????????${Constants.TIMER_NUM}???????????????", null))
        } else {
            _showDialog.value = Event(Resource.success(null))
        }
    }

    fun checkInputTimerName(timerName: String) {
        if (timerName.isEmpty()) {
            _nameStatus.value = Event(Resource.error("????????????????????????????????????????????????", timerName))
            return
        }

        if (timerName.length > Constants.MAX_NAME_LENGTH) {
            _nameStatus.value =
                Event(Resource.error("??????????????????${Constants.MAX_NAME_LENGTH}?????????????????????", timerName))
            return
        }

        if (timerNames.contains(timerName)) {
            _nameStatus.value = Event(Resource.error("?????????????????????????????????????????????????????????", timerName))
            return
        }
        _nameStatus.value = Event(Resource.success(timerName))
    }

    fun insertTimer(timerName: String) {
        val timer = Timer(timerName)
        insertTimerIntoDb(timer)
        _navigateToPresetTimer.value = Event(timer.name)
    }

    // test????????????????????????
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
            _navigateToTimer.value = Event(Resource.error("?????????????????????????????????????????????", null))
            return
        }
        _navigateToTimer.value = Event(Resource.success(timer.name))
    }

    fun navigateToDeleteTimer() {
        if (timerNames.count() == 0) {
            _navigateToDeleteTimer.value = Event(Resource.error("?????????????????????????????????????????????", null))
        } else {
            _navigateToDeleteTimer.value = Event(Resource.success(true))
        }
    }
}

@Suppress("UNCHECKED_CAST")
class TimerListViewModelFactory(
    private val timerRepository: TimerRepository
) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        (TimerListViewModel(timerRepository) as T)
}