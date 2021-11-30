package com.example.timerapp.ui

import androidx.lifecycle.*
import com.example.timerapp.database.*
import com.example.timerapp.others.*
import com.example.timerapp.repository.TimerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

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

    private val currentPresetTimer = MutableLiveData<PresetTimer>()

    private val _temporalPresetTime = MutableLiveData<Long>()
    val temporalPresetTime: LiveData<Long> = _temporalPresetTime

    private val _temporalNotificationTime = MutableLiveData<Long>()
    val temporalNotificationTime: LiveData<Long> = _temporalNotificationTime

    private val _deleteTimerItemStatus = MutableLiveData<Event<Resource<Timer>>>()
    val deleteTimerItemStatus: LiveData<Event<Resource<Timer>>> = _deleteTimerItemStatus
    private val deletedPresetTimerItems = MutableLiveData<List<PresetTimer>>()

    private val _deletePresetTimerStatus = MutableLiveData<Event<Resource<PresetTimer>>>()
    val deletePresetTimerStatus: LiveData<Event<Resource<PresetTimer>>> = _deletePresetTimerStatus

    private val _nameStatus = MutableLiveData<Event<Resource<String>>>()
    val nameStatus: LiveData<Event<Resource<String>>> = _nameStatus

    private val _updateTimerStatus = MutableLiveData<Event<Resource<Timer>>>()
    val updateTimerStatus: LiveData<Event<Resource<Timer>>> = _updateTimerStatus

    private val _insertAndUpdatePresetTimerStatus = MutableLiveData<Event<Resource<PresetTimer>>>()
    val insertAndUpdatePresetTimerStatus: LiveData<Event<Resource<PresetTimer>>> =
        _insertAndUpdatePresetTimerStatus

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

    // TimerListFragment(Home)
    // insert Timer
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

    fun checkInputTimerName(timerName: String) {
        if (timerName.isEmpty()) {
            _nameStatus.postValue(
                Event(Resource.error("タイマー名が入力されていません。", timerName))
            )
            return
        }

        if (timerName.length > Constants.MAX_NAME_LENGTH) {
            _nameStatus.postValue(
                Event(Resource.error("タイマー名は${Constants.MAX_NAME_LENGTH}文字までです。", timerName))
            )
            return
        }

        val timerNames = mutableListOf<String>()
        timerItems.value?.forEach { timerNames.add(it.name) }
        if (timerNames.contains(timerName)) {
            _nameStatus.postValue(
                Event(Resource.error("入力したタイマー名は使用されています。", timerName))
            )
            return
        }
        _nameStatus.postValue(Event(Resource.success(timerName)))
    }

    fun insertTimer(timerName: String) {
        val timer = Timer(timerName)
        getCurrentTimerAndPresetTimerList(timerName)
        insertTimerIntoDb(timer)
        _navigateToPresetTimer.value = timerName
    }

    // testに使用するため　
    fun insertTimerIntoDb(timer: Timer) {
        viewModelScope.launch {
            repository.insertTimer(timer)
        }
    }

    fun getCurrentTimerAndPresetTimerList(timerName: String) {
        viewModelScope.launch {
            _currentTimer.postValue(repository.getCurrentTimer(timerName))
            _presetTimerList.postValue(sortedOrder(repository.getPresetTimerWithTimer(timerName).presetTimer))
        }
    }

    private fun sortedOrder(presetTimerList: List<PresetTimer>): List<PresetTimer> {
        val comparator: Comparator<PresetTimer> = compareBy { it.timerOrder }
        return presetTimerList.sortedWith(comparator)
    }

    // swipe delete Timer
    fun deleteTimer(timer: Timer) {
        when (timer.detail) {
            "no presetTimer" -> deleteTimerFromDb(timer)
            else -> deleteTimerAndPresetTimers(timer)
        }
    }

    private fun deleteTimerFromDb(timer: Timer) {
        viewModelScope.launch {
            repository.deleteTimer(timer)
            _deleteTimerItemStatus.postValue(Event(Resource.success(timer)))
        }
    }

    private fun deleteTimerAndPresetTimers(timer: Timer) {
        viewModelScope.launch {
            val presetTimers = repository.getPresetTimerWithTimer(timer.name).presetTimer
            repository.deleteTimerAndPresetTimers(timer, presetTimers)
            deletedPresetTimerItems.postValue(presetTimers)
            _deleteTimerItemStatus.postValue(Event(Resource.success(timer)))
        }
    }

    // cancel deleteTimer
    fun restoreTimerAndPresetTimers(timer: Timer) {
        viewModelScope.launch {
            val presetTimers = deletedPresetTimerItems.value ?: listOf()
            repository.insertTimerAndPresetTimers(timer, presetTimers)
        }
    }

    // Navigation
    fun navigateToPresetTimer(name: String) {
        getCurrentTimerAndPresetTimerList(name)
        currentTimer.value?.let {
            if (it.name == name) {
                _navigateToPresetTimer.value = name
            }
        }
    }

    fun doneNavigateToPresetTimer() {
        _navigateToPresetTimer.value = null
    }

    fun navigateToTimer(name: String) {
        getCurrentTimerAndPresetTimerList(name)
        currentTimer.value?.let {
            if (it.total == 0L) {
                _showTimerError.postValue("タイマーが設定されていません。")
                return
            }

            if (it.name == name) {
                _navigateToTimer.value = name
            }
        }
    }

    fun doneNavigateToTimer() {
        _navigateToTimer.value = null
    }

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

    // PresetTimerListFragment
    // update Timer
    fun updateSettingTimer(notificationType: NotificationType) {
        val timer = currentTimer.value!!
        val newTimer = Timer(
            timer.name, timer.total, timer.listType, notificationType, timer.isDisplay, timer.detail
        )
        updateTimerIntoDb(newTimer)
        _updateTimerStatus.postValue(Event(Resource.success(newTimer)))
    }

    private fun updateTimerIntoDb(timer: Timer) {
        viewModelScope.launch {
            repository.updateTimer(timer)
            _currentTimer.postValue(repository.getCurrentTimer(timer.name))
        }
    }

    fun updateTimerNameAndSetting(
        timerName: String,
        notificationType: NotificationType,
    ) {
        currentTimer.value?.let { timer ->
            val newTimer = Timer(
                timerName,
                timer.total,
                timer.listType,
                notificationType,
                timer.isDisplay,
                timer.detail
            )
            updateTimerAndPresetTimers(newTimer)
        }
    }

    private fun updateTimerAndPresetTimers(newTimer: Timer) {
        viewModelScope.launch {
            currentTimer.value?.let { currentTimer ->
                val presetTimers = repository.getPresetTimerWithTimer(currentTimer.name).presetTimer
                val newPresetTimers = mutableListOf<PresetTimer>()
                presetTimers.forEach { presetTimer ->
                    newPresetTimers.add(
                        PresetTimer(
                            newTimer.name, presetTimer.presetName, presetTimer.timerOrder,
                            presetTimer.presetTime, presetTimer.notificationTime
                        )
                    )
                }
                repository.insertTimerAndPresetTimers(newTimer, newPresetTimers)
                repository.deleteTimerAndPresetTimers(currentTimer, presetTimers)
            }
            val timerAndPresetTimers = repository.getPresetTimerWithTimer(newTimer.name)
            _currentTimer.postValue(timerAndPresetTimers.timer)
            _presetTimerList.postValue(sortedOrder(timerAndPresetTimers.presetTimer))
            _updateTimerStatus.postValue(Event(Resource.success(newTimer)))
        }
    }

    // drag and drop updatePresetTimer
    fun changePresetTimerOrder(fromPos: Int, toPos: Int) {
        val presetTimerList = presetTimerList.value!!.toMutableList()
        if (fromPos < toPos) {
            val fromValue = presetTimerList[fromPos]
            presetTimerList.removeAt(fromPos)
            presetTimerList.add(toPos, fromValue)
        } else {
            val toValue = presetTimerList[toPos]
            presetTimerList.removeAt(toPos)
            presetTimerList.add(fromPos, toValue)
        }
        deleteAndInsertPresetTimers(presetTimerList)
    }

    private fun deleteAndInsertPresetTimers(presetTimerList: List<PresetTimer>) {
        val insertPresetTimerList = mutableListOf<PresetTimer>()
        presetTimerList.forEachIndexed { index, presetTimer ->
            insertPresetTimerList.add(
                PresetTimer(
                    presetTimer.name, presetTimer.presetName,
                    index + 1, presetTimer.presetTime, presetTimer.notificationTime
                )
            )
        }
        _presetTimerList.postValue(sortedOrder(insertPresetTimerList))

        viewModelScope.launch {
            repository.deletePresetTimers(presetTimerList)
            repository.insertPresetTimers(insertPresetTimerList)

            val updateTimer = timerChangeDueToPresetTimerChange(insertPresetTimerList)
            repository.updateTimer(updateTimer)
            _currentTimer.postValue(repository.getCurrentTimer(updateTimer.name))
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
            timer = Timer(it.name, total, listType, it.notificationType, it.isDisplay, detail)
        }
        return timer
    }

    // swipe delete PresetTimer
    fun deletePresetTimerAndUpdateTimer(presetTimer: PresetTimer) {
        viewModelScope.launch {
            repository.deletePresetTimer(presetTimer)
            currentTimer.value?.let { timer ->
                val presetTimerList =
                    sortedOrder(repository.getPresetTimerWithTimer(timer.name).presetTimer)
                val updateTimer = timerChangeDueToPresetTimerChange(presetTimerList)
                repository.updateTimer(updateTimer)
                val timerAndPresetTimers = repository.getPresetTimerWithTimer(timer.name)
                _currentTimer.postValue(timerAndPresetTimers.timer)
                _presetTimerList.postValue(sortedOrder(timerAndPresetTimers.presetTimer))
                _deletePresetTimerStatus.postValue(Event(Resource.success(presetTimer)))
            }
        }
    }

    fun restorePresetTimerAndUpdateTimer(presetTimer: PresetTimer) {
        viewModelScope.launch {
            repository.insertPresetTimer(presetTimer)
            currentTimer.value?.let {
                val presetTimerList =
                    sortedOrder(repository.getPresetTimerWithTimer(it.name).presetTimer)
                val updateTimer = timerChangeDueToPresetTimerChange(presetTimerList)
                repository.updateTimer(updateTimer)
                val timerAndPresetTimers = repository.getPresetTimerWithTimer(it.name)
                _currentTimer.postValue(timerAndPresetTimers.timer)
                _presetTimerList.postValue(sortedOrder(timerAndPresetTimers.presetTimer))
            }
        }
    }

    // Navigation
    fun navigateToSettingTimer(timerName: String?, presetName: String?, order: Int?) {
        val numberOfPresetTimers = presetTimerList.value?.count() ?: 0
        if (numberOfPresetTimers >= Constants.PRESET_TIMER_NUM) {
            _showTimerError.postValue("カスタマイズできるタイマーは${Constants.PRESET_TIMER_NUM}までです。")
            return
        }
        if (timerName != null && presetName != null && order != null) {
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

    // settingTimerFragmentとTimerFragmentに以降する際に必要
    fun getTemporalTime(timerName: String, presetName: String, order: Int) {
        viewModelScope.launch {
            val presetTimer = repository.getCurrentPresetTimer(timerName, presetName, order)
            currentPresetTimer.postValue(presetTimer)
            _temporalNotificationTime.postValue(presetTimer.notificationTime)
            _temporalPresetTime.postValue(presetTimer.presetTime)
        }
    }


    fun doneNavigateToSettingTimer() {
        _navigateToSettingTimer.value = null
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

    // SetTimerFragment
    // insert PresetTimer
    fun getTemporalNotificationTime(time: Long) {
        _temporalNotificationTime.postValue(time)
    }

    fun getTemporalPresetTime(time: Long) {
        _temporalPresetTime.postValue(time)
    }

    fun insertPresetTimer(presetName: String, presetTime: Long) {
        if (checkInputPresetTimer(presetName, presetTime)) {
            insertPresetTimerAndUpdateTimer(presetName, presetTime)
        } else {
            return
        }
    }

    private fun checkInputPresetTimer(presetName: String, presetTime: Long): Boolean {
        if (presetName.isEmpty()) {
            _nameStatus.postValue(
                Event(
                    Resource.error(
                        "タイマー名が入力されていません。", presetName
                    )
                )
            )
            return false
        }

        if (presetName.length > Constants.MAX_NAME_LENGTH) {
            _nameStatus.postValue(
                Event(
                    Resource.error(
                        "タイマー名は${Constants.MAX_NAME_LENGTH}文字までです。", presetName
                    )
                )
            )
            return false
        }

        if (presetTime <= (temporalNotificationTime.value ?: 0L) || presetTime == 0L) {
            _showTimerError.postValue("適切にタイマーの設定を行って下さい。")
            return false
        }

        _nameStatus.postValue(Event(Resource.success(null)))
        return true
    }


    private fun insertPresetTimerAndUpdateTimer(presetTimerName: String, presetTime: Long) {
        viewModelScope.launch {
            currentTimer.value?.let { timer ->
                val order = if (timer.detail == "no presetTimer") {
                    1
                } else {
                    repository.getMaxOrderPresetTimer(timer.name) + 1
                }
                val presetTimer = PresetTimer(
                    timer.name,
                    presetTimerName,
                    order,
                    presetTime,
                    temporalNotificationTime.value!!
                )
                repository.insertPresetTimer(presetTimer)

                val presetTimerList =
                    sortedOrder(repository.getPresetTimerWithTimer(timer.name).presetTimer)
                val updateTimer = timerChangeDueToPresetTimerChange(presetTimerList)
                repository.updateTimer(updateTimer)

                val timerAndPresetTimers = repository.getPresetTimerWithTimer(timer.name)
                _currentTimer.postValue(timerAndPresetTimers.timer)
                _presetTimerList.postValue(sortedOrder(timerAndPresetTimers.presetTimer))
                _insertAndUpdatePresetTimerStatus.postValue(Event(Resource.success(presetTimer)))
            }
        }
    }

    // update PresetTimer
    fun updatePresetTimer(presetName: String, presetTime: Long) {
        val presetTimer = currentPresetTimer.value!!
        val updatePresetTimer = PresetTimer(
            presetTimer.name, presetName, presetTimer.timerOrder,
            presetTime, temporalNotificationTime.value!!
        )
        if (checkInputPresetTimer(presetName, presetTime)) {
            if (presetName != presetTimer.presetName) {
                deleteAndInsertPresetTimerAndUpdateTimer(updatePresetTimer)
            } else {
                updatePresetTimerAndTimer(updatePresetTimer)
            }
            _insertAndUpdatePresetTimerStatus.postValue(Event(Resource.success(updatePresetTimer)))
        } else {
            return
        }
    }

    private fun deleteAndInsertPresetTimerAndUpdateTimer(presetTimer: PresetTimer) {
        viewModelScope.launch {
            currentPresetTimer.value?.let { repository.deletePresetTimer(it) }
            repository.insertPresetTimer(presetTimer)

            currentTimer.value?.let {
                val presetTimerList =
                    sortedOrder(repository.getPresetTimerWithTimer(it.name).presetTimer)
                val updateTimer = timerChangeDueToPresetTimerChange(presetTimerList)
                repository.updateTimer(updateTimer)

                val timerAndPresetTimers = repository.getPresetTimerWithTimer(it.name)
                _currentTimer.postValue(timerAndPresetTimers.timer)
                _presetTimerList.postValue(sortedOrder(timerAndPresetTimers.presetTimer))
            }
        }
    }

    private fun updatePresetTimerAndTimer(presetTimer: PresetTimer) {
        viewModelScope.launch {
            currentTimer.value?.let {
                repository.updatePresetTimers(listOf(presetTimer))
                val presetTimerList =
                    sortedOrder(repository.getPresetTimerWithTimer(it.name).presetTimer)
                val updateTimer = timerChangeDueToPresetTimerChange(presetTimerList)
                repository.updateTimer(updateTimer)

                val timerAndPresetTimers = repository.getPresetTimerWithTimer(it.name)
                _currentTimer.postValue(timerAndPresetTimers.timer)
                _presetTimerList.postValue(sortedOrder(timerAndPresetTimers.presetTimer))
            }
        }
    }

    // deleteTimerListFragment
    // deleteTimerList
    fun deleteTimerListAndPresetTimerList(timerList: List<Timer>) {
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

    // update Timer (select Delete Item)
    fun switchTimerIsSelected(timer: Timer) {
        viewModelScope.launch {
            repository.updateTimer(
                Timer(
                    timer.name, timer.total, timer.listType, timer.notificationType,
                    timer.isDisplay, timer.detail, !timer.isSelected
                )
            )
        }
    }

    fun cancelDeleteTimerList(timerList: List<Timer>) {
        viewModelScope.launch {
            val updateTimers = mutableListOf<Timer>()
            timerList.forEach {
                updateTimers.add(
                    Timer(
                        it.name,
                        it.total,
                        it.listType,
                        it.notificationType,
                        it.isDisplay,
                        it.detail,
                        false
                    )
                )
            }
            repository.updateTimers(updateTimers)
            _deleteTimerItemStatus.postValue(Event(Resource.success(null)))
        }
    }

    // deletePresetTimerListFragment
    fun deletePresetTimerList(deletePresetTimers: List<PresetTimer>) {
        viewModelScope.launch {
            repository.deletePresetTimers(deletePresetTimers)
            currentTimer.value?.let { timer ->
                val presetTimerList =
                    sortedOrder(repository.getPresetTimerWithTimer(timer.name).presetTimer)
                _presetTimerList.postValue(presetTimerList)
                val updateTimer = timerChangeDueToPresetTimerChange(presetTimerList)
                repository.updateTimer(updateTimer)
                _deletePresetTimerStatus.postValue(Event(Resource.success(null)))
            }
        }
    }

    // update presetTimer (select Delete Item)
    fun switchPresetTimerIsSelected(presetTimer: PresetTimer) {
        viewModelScope.launch {
            repository.updatePresetTimers(
                listOf(
                    PresetTimer(
                        presetTimer.name,
                        presetTimer.presetName,
                        presetTimer.timerOrder,
                        presetTimer.presetTime,
                        presetTimer.notificationTime,
                        !presetTimer.isSelected
                    )
                )
            )
            _presetTimerList.postValue(sortedOrder(repository.getPresetTimerWithTimer(presetTimer.name).presetTimer))
        }
    }

    fun cancelDeletePresetTimerList(presetTimerList: List<PresetTimer>) {
        viewModelScope.launch {
            val updatePresetTimers = mutableListOf<PresetTimer>()
            presetTimerList.forEach { presetTimer ->
                updatePresetTimers.add(
                    PresetTimer(
                        presetTimer.name, presetTimer.presetName, presetTimer.timerOrder,
                        presetTimer.presetTime, presetTimer.notificationTime, false
                    )
                )
            }
            repository.updatePresetTimers(updatePresetTimers)
        }
        _deletePresetTimerStatus.postValue(Event(Resource.success(null)))
    }

}

