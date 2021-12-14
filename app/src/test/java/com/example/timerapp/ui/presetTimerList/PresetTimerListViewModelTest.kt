package com.example.timerapp.ui.presetTimerList

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

class PresetTimerListViewModelTest {

    private lateinit var timerRepository: FakeTimerRepository
    private lateinit var presetTimerListViewModel: PresetTimerListViewModel

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()


    private val presetTime1 = PresetTimer("someTimer", "preset1", 1, 9000000)
    private val presetTime2 = PresetTimer("someTimer", "preset2", 2, 9000000)
    private val presetTime3 = PresetTimer("someTimer", "preset3", 3, 9000000)
    private val preset1 = PresetTimer("manyTimer", "preset1", 1, 600000)

    @Before
    fun setupViewModel() {
        val someTimer = Timer("someTimer", 27000000, ListType.SIMPLE_LAYOUT, NotificationType.VIBRATION, false,
            "preset1\t150分\t通知: なし\npreset2\t150分\t通知: なし\npreset3\t150分\t通知: なし\n")
        val manyTimer = Timer("manyTimer", 6000000, ListType.SIMPLE_LAYOUT, NotificationType.VIBRATION,
        false, "preset1\t10分\t通知: なし\npreset2\t10分\t通知: なし\npreset3\t10分\t通知: なし\n" +
                    "preset4\t10分\t通知: なし\npreset5\t10分\t通知: なし\npreset6\t10分\t通知: なし\n"+
                    "preset7\t10分\t通知: なし\npreset8\t10分\t通知: なし\npreset9\t10分\t通知: なし\npreset10\t10分\t通知: なし\n")
        val noPresetAlarm = Timer("noPresetAlarm", 0, ListType.SIMPLE_LAYOUT, NotificationType.ALARM)
        val noPresetVib = Timer("noPresetVib", 0, ListType.SIMPLE_LAYOUT, NotificationType.VIBRATION)
        val preset2 = PresetTimer("manyTimer", "preset2", 2, 600000)
        val preset3 = PresetTimer("manyTimer", "preset3", 3, 600000)
        val preset4 = PresetTimer("manyTimer", "preset4", 4, 600000)
        val preset5 = PresetTimer("manyTimer", "preset5", 5, 600000)
        val preset6 = PresetTimer("manyTimer", "preset1", 6, 600000)
        val preset7 = PresetTimer("manyTimer", "preset1", 7, 600000)
        val preset8 = PresetTimer("manyTimer", "preset1", 8, 600000)
        val preset9 = PresetTimer("manyTimer", "preset1", 9, 600000)
        val preset10 = PresetTimer("manyTimer", "preset1", 10, 600000)

        timerRepository = FakeTimerRepository()
        timerRepository.addTimers(someTimer, noPresetAlarm, noPresetVib, manyTimer)
        timerRepository.addPresetTimers(presetTime1, presetTime2, presetTime3, preset1, preset2, preset3,
        preset4, preset5, preset6, preset7, preset8, preset9, preset10)
        presetTimerListViewModel = PresetTimerListViewModel(timerRepository)
    }

    @Test
    fun updateTimer_setSoundVibration_returnSuccess() {
        presetTimerListViewModel.start("noPresetAlarm")
        presetTimerListViewModel.onItemSelectedNumber(0)
        presetTimerListViewModel.updateSettingTimer()

        val updateTimerStatus = presetTimerListViewModel.updateTimerStatus.getOrAwaitValue().getContentIfNotHandled()
        assertThat(updateTimerStatus?.status).isEqualTo(Status.SUCCESS)
        assertThat(updateTimerStatus?.data?.notificationType).isEqualTo(NotificationType.VIBRATION)
    }

    @Test
    fun updateTimer_setSoundAlarm_returnSuccess() {
        presetTimerListViewModel.start("noPresetVib")
        presetTimerListViewModel.onItemSelectedNumber(1)
        presetTimerListViewModel.updateSettingTimer()

        val updateTimerStatus = presetTimerListViewModel.updateTimerStatus.getOrAwaitValue().getContentIfNotHandled()
        assertThat(updateTimerStatus?.status).isEqualTo(Status.SUCCESS)
        assertThat(updateTimerStatus?.data?.notificationType).isEqualTo(NotificationType.ALARM)
    }

    @Test
    fun updateTimer_inputEmptyName_returnError() {
        presetTimerListViewModel.start("noPresetAlarm")
        presetTimerListViewModel.currentTimerName.value = ""
        presetTimerListViewModel.updateSettingTimer()

        val updateTimerStatus = presetTimerListViewModel.updateTimerStatus.getOrAwaitValue().getContentIfNotHandled()
        assertThat(updateTimerStatus?.status).isEqualTo(Status.ERROR)
    }

    @Test
    fun updateTimer_inputLongName_returnError() {
        presetTimerListViewModel.start("noPresetAlarm")
        presetTimerListViewModel.currentTimerName.value = "LongLongLongLongLongName"
        presetTimerListViewModel.updateSettingTimer()

        val updateTimerStatus = presetTimerListViewModel.updateTimerStatus.getOrAwaitValue().getContentIfNotHandled()
        assertThat(updateTimerStatus?.status).isEqualTo(Status.ERROR)
    }

    @Test
    fun updateTimer_inputUsedName_returnError() {
        presetTimerListViewModel.start("noPresetAlarm")
        presetTimerListViewModel.currentTimerName.value = "noPresetVib"
        presetTimerListViewModel.updateSettingTimer()

        val updateTimerStatus = presetTimerListViewModel.updateTimerStatus.getOrAwaitValue().getContentIfNotHandled()
        assertThat(updateTimerStatus?.status).isEqualTo(Status.ERROR)
    }

    @Test
    fun updateTimer_validInput_returnSuccess() {
        presetTimerListViewModel.start("noPresetAlarm")
        presetTimerListViewModel.onItemSelectedNumber(0)
        presetTimerListViewModel.currentTimerName.value = "timer"
        presetTimerListViewModel.updateSettingTimer()

        val updateTimerStatus = presetTimerListViewModel.updateTimerStatus.getOrAwaitValue().getContentIfNotHandled()
        assertThat(updateTimerStatus?.status).isEqualTo(Status.SUCCESS)
        assertThat(updateTimerStatus?.data?.name).isEqualTo("timer")
        assertThat(updateTimerStatus?.data?.notificationType).isEqualTo(NotificationType.VIBRATION)
    }

    @Test
    fun updateTimerAndPresetTimer_validInput_returnSuccess() {
        presetTimerListViewModel.start("someTimer")
        presetTimerListViewModel.onItemSelectedNumber(1)
        presetTimerListViewModel.currentTimerName.value = "timer"
        presetTimerListViewModel.updateSettingTimer()

        val updateTimerStatus = presetTimerListViewModel.updateTimerStatus.getOrAwaitValue().getContentIfNotHandled()
        assertThat(updateTimerStatus?.status).isEqualTo(Status.SUCCESS)
        assertThat(updateTimerStatus?.data?.name).isEqualTo("timer")
        assertThat(updateTimerStatus?.data?.notificationType).isEqualTo(NotificationType.ALARM)
    }

    @Test
    fun changeOrderPresetTimer_positionZeroToPositionTwo() {
        presetTimerListViewModel.start("someTimer")
        presetTimerListViewModel.changePresetTimerOrder(0,2)

        val presetTimerItems = presetTimerListViewModel.presetTimerList.getOrAwaitValue()
        assertThat(presetTimerItems).isEqualTo(listOf(
            PresetTimer(presetTime2.name, presetTime2.presetName, 1, presetTime2.presetTime,
                presetTime2.notificationTime, presetTime2.isSelected, presetTime2.presetTimerId),
            PresetTimer(presetTime3.name, presetTime3.presetName, 2, presetTime3.presetTime,
                presetTime3.notificationTime, presetTime3.isSelected, presetTime3.presetTimerId),
            PresetTimer(presetTime1.name, presetTime1.presetName, 3, presetTime1.presetTime,
                presetTime1.notificationTime, presetTime1.isSelected, presetTime1.presetTimerId)
        ))
    }

    @Test
    fun changeOrderPresetTimer_positionTwoToPositionZero() {
        presetTimerListViewModel.start("someTimer")
        presetTimerListViewModel.changePresetTimerOrder(2,0)

        val presetTimerItems = presetTimerListViewModel.presetTimerList.getOrAwaitValue()
        assertThat(presetTimerItems).isEqualTo(listOf(
            PresetTimer(presetTime2.name, presetTime2.presetName, 1, presetTime2.presetTime,
                presetTime2.notificationTime, presetTime2.isSelected, presetTime2.presetTimerId),
            PresetTimer(presetTime3.name, presetTime3.presetName, 2, presetTime3.presetTime,
                presetTime3.notificationTime, presetTime3.isSelected, presetTime3.presetTimerId),
            PresetTimer(presetTime1.name, presetTime1.presetName, 3, presetTime1.presetTime,
                presetTime1.notificationTime, presetTime1.isSelected, presetTime1.presetTimerId)))
    }

    @Test
    fun deletePresetTimer_deleteSuccess() {
        presetTimerListViewModel.start("someTimer")
        presetTimerListViewModel.deletePresetTimerAndUpdateTimer(presetTime1)

        val presetTimerItems = presetTimerListViewModel.presetTimerList.getOrAwaitValue()
        val deletePresetTimerStatus = presetTimerListViewModel.deletePresetTimerStatus.getOrAwaitValue().getContentIfNotHandled()
        assertThat(presetTimerItems).doesNotContain(presetTime1)
        assertThat(deletePresetTimerStatus?.status).isEqualTo(Status.SUCCESS)
    }

    @Test
    fun restoreDeletedPresetTimer_restoreSuccess() {
        presetTimerListViewModel.start("someTimer")
        presetTimerListViewModel.deletePresetTimerAndUpdateTimer(presetTime1)
        presetTimerListViewModel.restorePresetTimerAndUpdateTimer(presetTime1)

        val presetTimerItems = presetTimerListViewModel.presetTimerList.getOrAwaitValue()
        assertThat(presetTimerItems).contains(presetTime1)
    }

    @Test
    fun despiteOfManyTimerRegistration_navigateToSettingTimerForAdd_returnsError() {
        presetTimerListViewModel.start("manyTimer")
        presetTimerListViewModel.navigateToSettingTimer(null)

        val navigateToSettingTimer = presetTimerListViewModel.navigateToSettingTimer.getOrAwaitValue().getContentIfNotHandled()
        assertThat(navigateToSettingTimer?.status).isEqualTo(Status.ERROR)
    }

    @Test
    fun inTheCaseOfManyPresetTimerRegistration_navigateToSettingTimerForUpdate_returnsSuccess() {
        presetTimerListViewModel.start("manyTimer")
        presetTimerListViewModel.navigateToSettingTimer(preset1.presetTimerId)

        val navigateToSettingTimer = presetTimerListViewModel.navigateToSettingTimer.getOrAwaitValue().getContentIfNotHandled()
        assertThat(navigateToSettingTimer?.status).isEqualTo(Status.SUCCESS)
    }

    @Test
    fun inTheCaseSomePresetTimerRegistration_navigateToSettingTimerForAdd_returnsSuccess() {
        presetTimerListViewModel.start("someTimer")
        presetTimerListViewModel.navigateToSettingTimer(null)

        val navigateToSettingTimer = presetTimerListViewModel.navigateToSettingTimer.getOrAwaitValue().getContentIfNotHandled()
        assertThat(navigateToSettingTimer?.status).isEqualTo(Status.SUCCESS)
    }

    @Test
    fun despiteOfNoPresetTimerRegistration_navigateToDeletePresetTimer_returnError() {
        presetTimerListViewModel.start("noPresetAlarm")
        presetTimerListViewModel.navigateToDeletePresetTimer()

        val navigateToDeletePresetTimer = presetTimerListViewModel.navigateToDeletePresetTimer.getOrAwaitValue().getContentIfNotHandled()
        assertThat(navigateToDeletePresetTimer?.status).isEqualTo(Status.ERROR)
    }

    @Test
    fun inTheCaseOfSomePresetTimerRegistration_navigateToDeletePresetTimer_returnSuccess() {
        presetTimerListViewModel.start("someTimer")
        presetTimerListViewModel.navigateToDeletePresetTimer()

        val navigateToDeletePresetTimer = presetTimerListViewModel.navigateToDeletePresetTimer.getOrAwaitValue().getContentIfNotHandled()
        assertThat(navigateToDeletePresetTimer?.status).isEqualTo(Status.SUCCESS)
    }

    @Test
    fun navigateToTimerList_setAddEvent() {
        presetTimerListViewModel.start("someTimer")
        presetTimerListViewModel.navigateToTimerList()

        val navigateToTimerList = presetTimerListViewModel.navigateToTimerList.getOrAwaitValue().getContentIfNotHandled()
        assertThat(navigateToTimerList).isNotNull()
    }

    @Test
    fun despiteOfNoPresetTimerRegistration_navigateToTimer_returnError() {
        presetTimerListViewModel.start("noPresetAlarm")
        presetTimerListViewModel.navigateToTimer()

        val navigateToTimer = presetTimerListViewModel.navigateToTimer.getOrAwaitValue().getContentIfNotHandled()
        assertThat(navigateToTimer?.status).isEqualTo(Status.ERROR)
    }

    @Test
    fun despiteOfNoSomeTimerRegistration_navigateToTimer_returnSuccess() {
        presetTimerListViewModel.start("someTimer")
        presetTimerListViewModel.navigateToTimer()

        val navigateToTimer = presetTimerListViewModel.navigateToTimer.getOrAwaitValue().getContentIfNotHandled()
        assertThat(navigateToTimer?.status).isEqualTo(Status.SUCCESS)
    }
}