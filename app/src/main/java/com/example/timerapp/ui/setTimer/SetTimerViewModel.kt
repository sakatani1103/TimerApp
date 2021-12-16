package com.example.timerapp.ui.setTimer

import androidx.lifecycle.*
import com.example.timerapp.database.ListType
import com.example.timerapp.database.PresetTimer
import com.example.timerapp.database.Timer
import com.example.timerapp.others.*
import com.example.timerapp.repository.TimerRepository
import kotlinx.coroutines.launch

class SetTimerViewModel(private val timerRepository: TimerRepository) : ViewModel() {

    private val _insertAndUpdatePresetTimerStatus =
        MutableLiveData<Event<Resource<Map<String, String?>>>>()
    val insertAndUpdatePresetTimerStatus: LiveData<Event<Resource<Map<String, String?>>>> =
        _insertAndUpdatePresetTimerStatus

    private val _navigateToPresetTimer = MutableLiveData<Event<Boolean>>()
    val navigateToPresetTimer: LiveData<Event<Boolean>> = _navigateToPresetTimer

    private val _showDialog = MutableLiveData<Event<Boolean>>()
    val showDialog: LiveData<Event<Boolean>> = _showDialog

    private var currentTimer = MutableLiveData<Timer>()
    private var currentPresetTimer = MutableLiveData<PresetTimer>()
    private var presetTimerList = mutableListOf<PresetTimer>()

    var currentTimerName = MutableLiveData<String>()
    var currentPresetTime = 0L
    var currentNotificationTime = MutableLiveData(0L)
    var presetTimerType = DataType.INSERT

    // presetTime
    var min1 = MutableLiveData(0)
    var min2 = MutableLiveData(0)
    var min3 = MutableLiveData(0)
    var sec1 = MutableLiveData(0)
    var sec2 = MutableLiveData(0)

    // notificationTime
    var min = MutableLiveData(0)
    var sec = MutableLiveData(0)

    fun start(presetTimerId: String?, timerName: String) {
        viewModelScope.launch {
            val timerWithPresetTimer = timerRepository.getPresetTimerWithTimer(timerName)
            currentTimer.value = timerWithPresetTimer.timer
            presetTimerList = sortedOrder(timerWithPresetTimer.presetTimer).toMutableList()

            if (presetTimerId != null) {
                val presetTimer = timerRepository.getCurrentPresetTimer(presetTimerId)
                currentPresetTimer.value = presetTimer
                currentTimerName.value = presetTimer.presetName
                currentPresetTime = presetTimer.presetTime
                setPresetTimeNumberPicker(currentPresetTime)
                currentNotificationTime.value = presetTimer.notificationTime
                setNotificationTimeNumberPicker(presetTimer.notificationTime)
                presetTimerType = DataType.UPDATE
            } else {
                val order = if (presetTimerList.count() == 0) 1
                 else { presetTimerList.last().timerOrder + 1 }
                currentTimerName.value = "presetTimer$order"
            }
        }
    }

    fun savePresetTimer() {
        val presetName = currentTimerName.value!!
        currentPresetTime = getPresetTimeFromNumberPicker(
            min1.value!!,
            min2.value!!,
            min3.value!!,
            sec1.value!!,
            sec2.value!!
        )
        val notificationTime = currentNotificationTime.value ?: 0L

        if (presetName.isEmpty()) {
            _insertAndUpdatePresetTimerStatus.value = Event(
                Resource.error(
                    "タイマー名が入力されていません。", mapOf("error" to "name", "value" to presetName)
                )
            )
            return
        }

        if (presetName.length > Constants.MAX_NAME_LENGTH) {
            _insertAndUpdatePresetTimerStatus.value = Event(
                Resource.error(
                    "タイマー名は${Constants.MAX_NAME_LENGTH}文字までです。",
                    mapOf("error" to "name", "value" to presetName)
                )
            )
            return
        }


        if (currentPresetTime <= notificationTime || currentPresetTime == 0L) {
            _insertAndUpdatePresetTimerStatus.value = Event(
                Resource.error("適切にタイマーの設定を行って下さい。", mapOf("error" to "time", "value" to null))
            )
            return
        }

        when (presetTimerType) {
            DataType.INSERT -> insertPresetTimerAndUpdateTimerIntoDb(
                presetName,
                currentPresetTime,
                notificationTime
            )
            DataType.UPDATE -> updatePresetTimerAndTimerIntoDb(
                presetName,
                currentPresetTime,
                notificationTime
            )
        }
    }


    private fun insertPresetTimerAndUpdateTimerIntoDb(
        presetTimerName: String,
        presetTime: Long,
        notificationTime: Long
    ) {
        viewModelScope.launch {
            currentTimer.value?.let { timer ->
                val order = if ( presetTimerList.count() == 0) 1
                else { presetTimerList.last().timerOrder + 1}
                val presetTimer = PresetTimer(
                    timer.name,
                    presetTimerName,
                    order,
                    presetTime,
                    notificationTime
                )
                timerRepository.insertPresetTimer(presetTimer)
                presetTimerList.add(presetTimer)
                val updateTimer = timerChangeDueToPresetTimerChange(presetTimerList)
                timerRepository.updateTimer(updateTimer)

                _insertAndUpdatePresetTimerStatus.value = Event(
                    Resource.success(mapOf("value" to timer.name))
                )
            }
        }
    }

    private fun sortedOrder(presetTimerList: List<PresetTimer>): List<PresetTimer> {
        val comparator: Comparator<PresetTimer> = compareBy { it.timerOrder }
        return presetTimerList.sortedWith(comparator)
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

    private fun updatePresetTimerAndTimerIntoDb(
        presetTimerName: String,
        presetTime: Long,
        notificationTime: Long
    ) {
        viewModelScope.launch {
            currentPresetTimer.value?.let { preset ->
                val updatePresetTimer = PresetTimer(
                    preset.name,
                    presetTimerName,
                    preset.timerOrder,
                    presetTime,
                    notificationTime,
                    false,
                    preset.presetTimerId
                )
                timerRepository.updatePresetTimer(updatePresetTimer)
                presetTimerList.remove(preset)
                presetTimerList.add(updatePresetTimer)
                presetTimerList = sortedOrder(presetTimerList).toMutableList()
            }
            val updateTimer = timerChangeDueToPresetTimerChange(presetTimerList)
            timerRepository.updateTimer(updateTimer)
            _insertAndUpdatePresetTimerStatus.value = Event(
                Resource.success(mapOf("value" to updateTimer.name))
            )
        }
    }

    private fun setPresetTimeNumberPicker(currentPresetTime: Long) {
        val numMap = setIntToNumberPicker(currentPresetTime)
        min1.value = numMap[1]?.toInt()
        min2.value = numMap[2]?.toInt()
        min3.value = numMap[3]?.toInt()
        sec1.value = numMap[4]?.toInt()
        sec2.value = numMap[5]?.toInt()
    }

    private fun setNotificationTimeNumberPicker(currentNotificationTime: Long) {
        val numMap = setPreNotification(currentNotificationTime)
        min.value = numMap["min"]?.toInt()
        sec.value = numMap["sec"]?.toInt()
    }

    fun updateCurrentNotification() {
        currentNotificationTime.value = getNotificationFromNumberPicker(min.value!!, sec.value!!)
    }

    fun showDialog() {
        _showDialog.value = Event(true)
    }

    fun navigateToPresetTimer() {
        _navigateToPresetTimer.value = Event(true)
    }
}

enum class DataType { INSERT, UPDATE }

@Suppress("UNCHECKED_CAST")
class SetTimerViewModelFactory(

    private val timerRepository: TimerRepository
) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        (SetTimerViewModel(timerRepository) as T)
}