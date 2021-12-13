package com.example.timerapp.ui.timer

import android.os.CountDownTimer
import androidx.lifecycle.*
import com.example.timerapp.database.NotificationType
import com.example.timerapp.database.PresetTimer
import com.example.timerapp.others.Event
import com.example.timerapp.repository.TimerRepository
import kotlinx.coroutines.launch

class TimerViewModel(private val timerRepository: TimerRepository) : ViewModel() {

    private val _notifyPreNotification = MutableLiveData<Event<NotificationType>>()
    val notifyPreNotification : LiveData<Event<NotificationType>> = _notifyPreNotification

    private val _notifyPresetTimerFinish = MutableLiveData<Event<NotificationType>>()
    val notifyPresetTimerFinish : LiveData<Event<NotificationType>> = _notifyPresetTimerFinish

    private val _notifyTimerFinish = MutableLiveData<Event<NotificationType>>()
    val notifyTimerFinish : LiveData<Event<NotificationType>> = _notifyTimerFinish

    private var residuePresetTimerList = mutableListOf<PresetTimer>()
    var presetTimerTitle = MutableLiveData<String>()
    var residueMin = MutableLiveData(0L)
    var residueSec = MutableLiveData(0L)
    var residueTotalMin = MutableLiveData(0L)
    var residueTotalSec = MutableLiveData(0L)

    private var notificationType = NotificationType.VIBRATION
    private var resumeFromMillis: Long = 0
    private var others = 0L
    private var preNotificationTime = 0L
    private var isNotification = false

    private var timerStatus = Status.PROGRESS

    fun start(timerName: String) {
        viewModelScope.launch {
            val timerWithPresetTimers = timerRepository.getPresetTimerWithTimer(timerName)
            residuePresetTimerList = timerWithPresetTimers.presetTimer.toMutableList()
            others = timerWithPresetTimers.timer.total
            notificationType = timerWithPresetTimers.timer.notificationType
            setFirstElement()
        }
    }

    inner class CustomCountDownTimer(
        millisInFuture: Long, countDownInterval: Long
    ) : CountDownTimer(millisInFuture, countDownInterval) {
        override fun onTick(millisUtilFinished: Long) {
            when (timerStatus) {
                Status.PAUSE -> {
                    resumeFromMillis = millisUtilFinished
                    cancel()
                    displayTime(millisUtilFinished)
                }
                else -> {
                    displayTime(millisUtilFinished)
                }
            }
            if (preNotificationTime > 0 && preNotificationTime >= millisUtilFinished && !isNotification) {
                notifyPreNotificationTime()
                isNotification = true
            }
        }

        override fun onFinish() {
            residueMin.value = 0L
            residueSec.value = 0L
            residuePresetTimerList.removeAt(0)
            if (residuePresetTimerList.isEmpty()) {
                notifyTimerFinish()
            } else {
                notifyPresetTimerFinish()
                setFirstElement()
            }
        }
    }

    private fun displayTime(millisUtilFinished: Long) {
        residueMin.value = millisUtilFinished / 1000L / 60L
        residueSec.value = millisUtilFinished / 1000L % 60L
        residueTotalMin.value = (others + millisUtilFinished) / 1000L / 60L
        residueTotalSec.value = (others + millisUtilFinished) / 1000L % 60L
    }

    private fun setFirstElement() {
        presetTimerTitle.value = residuePresetTimerList.first().presetName
        resumeFromMillis = residuePresetTimerList.first().presetTime
        others -= residuePresetTimerList.first().presetTime
        preNotificationTime = residuePresetTimerList.first().notificationTime
        isNotification = false
        val customCountDownTimer = CustomCountDownTimer(resumeFromMillis, 100)
        customCountDownTimer.start()
    }

    private fun notifyPreNotificationTime(){
        _notifyPreNotification.value = Event(notificationType)
    }

    private fun notifyPresetTimerFinish(){
        _notifyPresetTimerFinish.value = Event(notificationType)
    }

    private fun notifyTimerFinish(){
        _notifyTimerFinish.value = Event(notificationType)
    }

    fun timerPause() {
        timerStatus = Status.PAUSE
    }

    fun timerStart() {
        timerStatus = Status.PROGRESS
        val customCountDownTimer = CustomCountDownTimer(resumeFromMillis, 100)
        customCountDownTimer.start()
    }
}

enum class Status { PROGRESS, PAUSE }

@Suppress("UNCHECKED_CAST")
class TimerViewModelFactory(
    private val timerRepository: TimerRepository
) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        (TimerViewModel(timerRepository) as T)
}