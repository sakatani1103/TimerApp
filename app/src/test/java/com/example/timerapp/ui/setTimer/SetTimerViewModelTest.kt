package com.example.timerapp.ui.setTimer

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.timerapp.database.ListType
import com.example.timerapp.database.NotificationType
import com.example.timerapp.database.PresetTimer
import com.example.timerapp.database.Timer
import com.example.timerapp.getOrAwaitValue
import com.example.timerapp.others.Status
import com.example.timerapp.repository.FakeTimerRepository
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SetTimerViewModelTest {

    private lateinit var timerRepository: FakeTimerRepository
    private lateinit var setTimerViewModel: SetTimerViewModel

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private val presetTime1 = PresetTimer("someTimer", "preset1", 1, 9000000)

    @Before
    fun setupViewModel() {
        val timer = Timer("timer", 27000000, ListType.SIMPLE_LAYOUT, NotificationType.VIBRATION, false,
            "preset1\t150分\t通知: なし\npreset2\t150分\t通知: なし\npreset3\t150分\t通知: なし\n")
        val presetTime2 = PresetTimer("someTimer", "preset2", 2, 9000000)
        val presetTime3 = PresetTimer("someTimer", "preset3", 3, 9000000)

        timerRepository = FakeTimerRepository()
        timerRepository.addTimers(timer)
        timerRepository.addPresetTimers(presetTime1, presetTime2, presetTime3)
        setTimerViewModel = SetTimerViewModel(timerRepository)
    }

    @Test
    fun inTheCaseOfAdd_inputNameIsEmpty_returnsError() {
        setTimerViewModel.start(null, "timer")
        setTimerViewModel.currentTimerName.value = ""
        // set 150 min Number Picker
        setTimerViewModel.min1.value = 1
        setTimerViewModel.min2.value = 5
        setTimerViewModel.min3.value = 1

        setTimerViewModel.savePresetTimer()
        val insertAndUpdatePresetTimerStatus = setTimerViewModel.insertAndUpdatePresetTimerStatus.getOrAwaitValue().getContentIfNotHandled()
        assertThat(insertAndUpdatePresetTimerStatus?.status).isEqualTo(Status.ERROR)
    }

    @Test
    fun inTheCaseOfAdd_inputLongName_returnsError() {
        setTimerViewModel.start(null, "timer")
        setTimerViewModel.currentTimerName.value = "LongLongLongLongLongName"
        // set 150 min Number Picker
        setTimerViewModel.min1.value = 1
        setTimerViewModel.min2.value = 5
        setTimerViewModel.min3.value = 0

        setTimerViewModel.savePresetTimer()
        val insertAndUpdatePresetTimerStatus = setTimerViewModel.insertAndUpdatePresetTimerStatus.getOrAwaitValue().getContentIfNotHandled()
        assertThat(insertAndUpdatePresetTimerStatus?.status).isEqualTo(Status.ERROR)
    }

    @Test
    fun inTheCaseOfAdd_inputLongerPreNotificationTimeThanPresetTime_returnsError() {
        setTimerViewModel.start(null, "timer")
        // set 10 min Number Picker
        setTimerViewModel.min1.value = 0
        setTimerViewModel.min2.value = 1
        setTimerViewModel.min3.value = 0

        // set 15 min Number Picker Of PreNotification
        setTimerViewModel.min.value = 15
        setTimerViewModel.updateCurrentNotification()

        setTimerViewModel.savePresetTimer()
        val insertAndUpdatePresetTimerStatus = setTimerViewModel.insertAndUpdatePresetTimerStatus.getOrAwaitValue().getContentIfNotHandled()
        assertThat(insertAndUpdatePresetTimerStatus?.status).isEqualTo(Status.ERROR)
    }

    @Test
    fun inTheCaseOfAdd_validInput_returnsSuccess() {
        setTimerViewModel.start(null, "timer")
        setTimerViewModel.currentTimerName.value = "newTimer"

        // set 10 min Number Picker
        setTimerViewModel.min1.value = 1
        setTimerViewModel.min2.value = 5
        setTimerViewModel.min3.value = 0

        // set 15 min Number Picker Of PreNotification
        setTimerViewModel.min.value = 15
        setTimerViewModel.updateCurrentNotification()

        setTimerViewModel.savePresetTimer()
        val insertAndUpdatePresetTimerStatus = setTimerViewModel.insertAndUpdatePresetTimerStatus.getOrAwaitValue().getContentIfNotHandled()
        assertThat(insertAndUpdatePresetTimerStatus?.status).isEqualTo(Status.SUCCESS)
    }

    @Test
    fun inTheCaseOfUpdate_inputNameIsEmpty_returnsError() {
        setTimerViewModel.start(presetTime1.presetTimerId, "timer")
        setTimerViewModel.currentTimerName.value = ""
        // set 150 min Number Picker
        setTimerViewModel.min1.value = 1
        setTimerViewModel.min2.value = 5
        setTimerViewModel.min3.value = 1

        setTimerViewModel.savePresetTimer()
        val insertAndUpdatePresetTimerStatus = setTimerViewModel.insertAndUpdatePresetTimerStatus.getOrAwaitValue().getContentIfNotHandled()
        assertThat(insertAndUpdatePresetTimerStatus?.status).isEqualTo(Status.ERROR)
    }

    @Test
    fun inTheCaseOfUpdate_inputLongName_returnsError() {
        setTimerViewModel.start(presetTime1.presetTimerId, "timer")
        setTimerViewModel.currentTimerName.value = "LongLongLongLongLongName"
        // set 150 min Number Picker
        setTimerViewModel.min1.value = 1
        setTimerViewModel.min2.value = 5
        setTimerViewModel.min3.value = 0

        setTimerViewModel.savePresetTimer()
        val insertAndUpdatePresetTimerStatus = setTimerViewModel.insertAndUpdatePresetTimerStatus.getOrAwaitValue().getContentIfNotHandled()
        assertThat(insertAndUpdatePresetTimerStatus?.status).isEqualTo(Status.ERROR)
    }

    @Test
    fun inTheCaseOfUpdate_inputLongerPreNotificationTimeThanPresetTime_returnsError() {
        setTimerViewModel.start(presetTime1.presetTimerId, "timer")
        // set 10 min Number Picker
        setTimerViewModel.min1.value = 0
        setTimerViewModel.min2.value = 1
        setTimerViewModel.min3.value = 0

        // set 15 min Number Picker Of PreNotification
        setTimerViewModel.min.value = 15
        setTimerViewModel.updateCurrentNotification()

        setTimerViewModel.savePresetTimer()
        val insertAndUpdatePresetTimerStatus = setTimerViewModel.insertAndUpdatePresetTimerStatus.getOrAwaitValue().getContentIfNotHandled()
        assertThat(insertAndUpdatePresetTimerStatus?.status).isEqualTo(Status.ERROR)
    }

    @Test
    fun inTheCaseOfUpdate_validInput_returnsSuccess() {
        setTimerViewModel.start(presetTime1.presetTimerId, "timer")
        setTimerViewModel.currentTimerName.value = "newTimer"

        // set 10 min Number Picker
        setTimerViewModel.min1.value = 1
        setTimerViewModel.min2.value = 5
        setTimerViewModel.min3.value = 0

        // set 15 min Number Picker Of PreNotification
        setTimerViewModel.min.value = 15
        setTimerViewModel.updateCurrentNotification()

        setTimerViewModel.savePresetTimer()
        val insertAndUpdatePresetTimerStatus = setTimerViewModel.insertAndUpdatePresetTimerStatus.getOrAwaitValue().getContentIfNotHandled()
        assertThat(insertAndUpdatePresetTimerStatus?.status).isEqualTo(Status.SUCCESS)
    }

    @Test
    fun navigateToPresetTimer_setEvent() {
        setTimerViewModel.start(presetTime1.presetTimerId, "timer")
        setTimerViewModel.navigateToPresetTimer()
        val navigateToPresetTimer = setTimerViewModel.navigateToPresetTimer.getOrAwaitValue().getContentIfNotHandled()
        assertThat(navigateToPresetTimer).isNotNull()
    }

    @Test
    fun showDialog_setEvent() {
        setTimerViewModel.start(presetTime1.presetTimerId, "timer")
        setTimerViewModel.showDialog()
        val showDialog = setTimerViewModel.showDialog.getOrAwaitValue().getContentIfNotHandled()
        assertThat(showDialog).isNotNull()

    }

}