package com.example.timerapp.ui.timerlist

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

class TimerListViewModelTest {

    private lateinit var timerRepository: FakeTimerRepository
    private lateinit var timerListViewModel: TimerListViewModel

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private val timer1 = Timer("timer1")
    private val timer2 = Timer("timer2", 900000, ListType.SIMPLE_LAYOUT, NotificationType.VIBRATION, false,
    "通知: なし")
    private val presetTimer = PresetTimer("timer2", "preset", 1, 900000)

    @Before
    fun setupViewModel() {
        timerRepository = FakeTimerRepository()
        timerRepository.addTimers(timer1, timer2)
        timerRepository.addPresetTimers(presetTimer)
        timerListViewModel = TimerListViewModel(timerRepository)
    }

    @Test
    fun despiteOfManyTimers_showDialog_returnsError() {
        timerListViewModel.insertTimer("timer3")
        timerListViewModel.insertTimer("timer4")
        timerListViewModel.insertTimer("timer5")
        timerListViewModel.insertTimer("timer6")
        timerListViewModel.insertTimer("timer7")
        timerListViewModel.insertTimer("timer8")
        timerListViewModel.insertTimer("timer9")
        timerListViewModel.insertTimer("timer10")
        timerListViewModel.insertTimer("timer11")
        timerListViewModel.insertTimer("timer12")
        timerListViewModel.insertTimer("timer13")
        timerListViewModel.insertTimer("timer14")
        timerListViewModel.insertTimer("timer15")
        timerListViewModel.start()
        timerListViewModel.createInsertTimerDialog()
        val showDialog = timerListViewModel.showDialog.getOrAwaitValue().getContentIfNotHandled()
        assertThat(showDialog?.status).isEqualTo(Status.ERROR)
    }

    @Test
    fun showDialog_returnsSuccess(){
        timerListViewModel.start()
        timerListViewModel.createInsertTimerDialog()
        val showDialog = timerListViewModel.showDialog.getOrAwaitValue().getContentIfNotHandled()
        assertThat(showDialog?.status).isEqualTo(Status.SUCCESS)
    }

    @Test
    fun checkInputTimerName_inputEmptyName_returnsError() {
        timerListViewModel.start()
        timerListViewModel.checkInputTimerName("")
        val nameStatus = timerListViewModel.nameStatus.getOrAwaitValue().getContentIfNotHandled()
        assertThat(nameStatus?.status).isEqualTo(Status.ERROR)
    }

    @Test
    fun checkInputTimerName_inputLongName_returnsError() {
        timerListViewModel.start()
        timerListViewModel.checkInputTimerName("longLongLongLongLongName")
        val nameStatus = timerListViewModel.nameStatus.getOrAwaitValue().getContentIfNotHandled()
        assertThat(nameStatus?.status).isEqualTo(Status.ERROR)
    }

    @Test
    fun checkInputTimerName_inputUsedName_returnsError() {
        timerListViewModel.start()
        timerListViewModel.checkInputTimerName("timer1")
        val nameStatus = timerListViewModel.nameStatus.getOrAwaitValue().getContentIfNotHandled()
        assertThat(nameStatus?.status).isEqualTo(Status.ERROR)
    }

    @Test
    fun checkInputTimerName_inputValidName_returnsError() {
        timerListViewModel.start()
        timerListViewModel.checkInputTimerName("timer15")
        val nameStatus = timerListViewModel.nameStatus.getOrAwaitValue().getContentIfNotHandled()
        assertThat(nameStatus?.status).isEqualTo(Status.SUCCESS)
    }

    @Test
    fun deleteTimer_notRegisteredPresetTimer_returnSuccess() {
        timerListViewModel.start()
        timerListViewModel.deleteTimer(timer1)
        val timerList = timerListViewModel.timerItems.getOrAwaitValue()
        val deleteTimerItemStatus = timerListViewModel.deleteTimerItemStatus.getOrAwaitValue().getContentIfNotHandled()
        assertThat(timerList).doesNotContain(timer1)
        assertThat(deleteTimerItemStatus?.status).isEqualTo(Status.SUCCESS)
    }

    @Test
    fun deleteTimer_RegisteredPresetTimer_returnSuccess() {
        timerListViewModel.start()
        timerListViewModel.deleteTimer(timer2)
        val timerList = timerListViewModel.timerItems.getOrAwaitValue()
        val presetTimerList = timerListViewModel.presetTimers.getOrAwaitValue()
        val deleteTimerItemStatus = timerListViewModel.deleteTimerItemStatus.getOrAwaitValue().getContentIfNotHandled()
        assertThat(timerList).doesNotContain(timer2)
        assertThat(presetTimerList).doesNotContain(presetTimer)
        assertThat(deleteTimerItemStatus?.status).isEqualTo(Status.SUCCESS)
    }

    @Test
    fun restoreDeletedTimer_notRegisteredPresetTimer() {
        timerListViewModel.start()
        timerListViewModel.deleteTimer(timer1)
        timerListViewModel.restoreTimerAndPresetTimers(timer1)
        val timerList = timerListViewModel.timerItems.getOrAwaitValue()
        assertThat(timerList).contains(timer1)
    }

    @Test
    fun restoreDeletedTimer_RegisteredPresetTimer() {
        timerListViewModel.start()
        timerListViewModel.deleteTimer(timer2)
        timerListViewModel.restoreTimerAndPresetTimers(timer2)
        val timerList = timerListViewModel.timerItems.getOrAwaitValue()
        val presetTimerList = timerListViewModel.presetTimers.getOrAwaitValue()
        assertThat(timerList).contains(timer2)
        assertThat(presetTimerList).contains(presetTimer)
    }

    @Test
    fun inTheCaseOfNewRegistration_NavigateToPresetTimer_setNewTimerName() {
        timerListViewModel.start()
        timerListViewModel.insertTimer("timer3")
        val navigateToPresetTimer = timerListViewModel.navigateToPresetTimer.getOrAwaitValue().getContentIfNotHandled()
        assertThat(navigateToPresetTimer).isEqualTo("timer3")
    }

    @Test
    fun inTheCaseOfCustom_NavigateToPresetTimer_setSelectedTimerName() {
        timerListViewModel.start()
        timerListViewModel.navigateToPresetTimer("timer1")
        val navigateToPresetTimer = timerListViewModel.navigateToPresetTimer.getOrAwaitValue().getContentIfNotHandled()
        assertThat(navigateToPresetTimer).isEqualTo("timer1")
    }

    @Test
    fun inTheCaseOfNoTimer_NavigateToDeleteTimer_returnError() {
        timerListViewModel.deleteTimer(timer1)
        timerListViewModel.deleteTimer(timer2)
        timerListViewModel.start()
        timerListViewModel.navigateToDeleteTimer()
        val navigateToDeleteTimer = timerListViewModel.navigateToDeleteTimer.getOrAwaitValue().getContentIfNotHandled()
        assertThat(navigateToDeleteTimer?.status).isEqualTo(Status.ERROR)
    }

    @Test
    fun inTheCaseOfSomeTimer_NavigateToDeleteTimer_returnSuccess() {
        timerListViewModel.start()
        timerListViewModel.navigateToDeleteTimer()
        val navigateToDeleteTimer = timerListViewModel.navigateToDeleteTimer.getOrAwaitValue().getContentIfNotHandled()
        assertThat(navigateToDeleteTimer?.status).isEqualTo(Status.SUCCESS)
    }

    @Test
    fun inTheCaseOfNoPresetTimer_NavigateToTimer_returnError(){
        timerListViewModel.start()
        timerListViewModel.navigateToTimer(timer1)
        val navigateToTimer = timerListViewModel.navigateToTimer.getOrAwaitValue().getContentIfNotHandled()
        assertThat(navigateToTimer?.status).isEqualTo(Status.ERROR)
    }

    @Test
    fun inTheCaseOfSomePresetTimer_NavigateToTimer_returnSuccess(){
        timerListViewModel.start()
        timerListViewModel.navigateToTimer(timer2)
        val navigateToTimer = timerListViewModel.navigateToTimer.getOrAwaitValue().getContentIfNotHandled()
        assertThat(navigateToTimer?.status).isEqualTo(Status.SUCCESS)
    }
}