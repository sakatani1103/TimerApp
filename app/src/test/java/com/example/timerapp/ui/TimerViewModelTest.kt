package com.example.timerapp.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.timerapp.MainCoroutineRule
import com.example.timerapp.database.ListType
import com.example.timerapp.database.NotificationType
import com.example.timerapp.database.PresetTimer
import com.example.timerapp.database.Timer
import com.example.timerapp.getOrAwaitValue
import com.example.timerapp.others.Constants
import com.example.timerapp.others.Status
import com.example.timerapp.repository.FakeTimerRepository
import com.google.common.truth.Truth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test


@ExperimentalCoroutinesApi
class TimerViewModelTest {
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var timerViewModel: TimerViewModel
    private lateinit var timerRepository: FakeTimerRepository

    @Before
    fun setup() {
        timerRepository = FakeTimerRepository()
        val timer1 = Timer("timer1")
        val timer2 = Timer("timer2")
        timerRepository.addTimers(timer1, timer2)
        timerViewModel = TimerViewModel(timerRepository)
        val name = "timer1"
        timerViewModel.getCurrentTimerAndPresetTimerList(name)
    }

    // TimerListFragment
    // insert
    @Test
    fun `insert timer with empty name, return error`() {
        timerViewModel.checkInputTimerName("")
        val value = timerViewModel.nameStatus.getOrAwaitValue()
        Truth.assertThat(value.getContentIfNotHandled()?.status).isEqualTo(Status.ERROR)
    }

    @Test
    fun `insert timer with too long name, return error`() {
        timerViewModel.checkInputTimerName("VeryVeryVeryLongNameTimer")
        val value = timerViewModel.nameStatus.getOrAwaitValue()
        Truth.assertThat(value.getContentIfNotHandled()?.status).isEqualTo(Status.ERROR)
    }

    @Test
    fun `insert timer with registered name, return error`() {
        timerViewModel.checkInputTimerName("timer1")
        val value = timerViewModel.nameStatus.getOrAwaitValue()
        Truth.assertThat(value.getContentIfNotHandled()?.status).isEqualTo(Status.ERROR)
    }

    @Test
    fun `insert timer with valid input, return success`() {
        timerViewModel.checkInputTimerName("timer")
        val value = timerViewModel.nameStatus.getOrAwaitValue()
        Truth.assertThat(value.getContentIfNotHandled()?.status).isEqualTo(Status.SUCCESS)
    }

    @Test
    fun `insert many timers, cannot create input TimerName Dialog`(){
        timerViewModel.insertTimerIntoDb(Timer("timer3"))
        timerViewModel.insertTimerIntoDb(Timer("timer4"))
        timerViewModel.insertTimerIntoDb(Timer("timer5"))
        timerViewModel.insertTimerIntoDb(Timer("timer6"))
        timerViewModel.insertTimerIntoDb(Timer("timer7"))
        timerViewModel.insertTimerIntoDb(Timer("timer8"))
        timerViewModel.insertTimerIntoDb(Timer("timer9"))
        timerViewModel.insertTimerIntoDb(Timer("timer10"))
        timerViewModel.insertTimerIntoDb(Timer("timer11"))
        timerViewModel.insertTimerIntoDb(Timer("timer12"))
        timerViewModel.insertTimerIntoDb(Timer("timer13"))
        timerViewModel.insertTimerIntoDb(Timer("timer14"))
        timerViewModel.insertTimerIntoDb(Timer("timer15"))
        timerViewModel.insertTimerIntoDb(Timer("timer16"))
        timerViewModel.createInsertTimerDialog()
        val value = timerViewModel.showTimerError.getOrAwaitValue()
        Truth.assertThat(value).isEqualTo("登録できるタイマーは${Constants.TIMER_NUM}までです。")
    }

    // swipe delete Timer
    @Test
    fun `swipe and delete timer, return success`(){
        val timer1 = Timer("timer1")
        timerViewModel.deleteTimer(timer1)
        val value = timerViewModel.deleteTimerItemStatus.getOrAwaitValue()
        Truth.assertThat(value.getContentIfNotHandled()?.status).isEqualTo(Status.SUCCESS)
    }

    @Test
    fun `swipe timer, delete correct timer`(){
        val timer1 = Timer("timer1")
        timerViewModel.deleteTimer(timer1)
        val allTimerList = timerViewModel.timerItems.getOrAwaitValue()
        Truth.assertThat(allTimerList.contains(timer1)).isFalse()
    }

    @Test
    fun `swipe and delete timer accompanied by deletion of preset timers`(){
        val timer1 = Timer("timer1")
        timerViewModel.getTemporalNotificationTime(600000)
        timerViewModel.insertPresetTimer("preset1", 9000000)

        timerViewModel.deleteTimer(timer1)
        timerViewModel.getCurrentTimerAndPresetTimerList("timer1")
        val presetTimerList = timerViewModel.presetTimerList.getOrAwaitValue()
        val presetTimer = PresetTimer("timer1", "preset1", 1, 9000000, 600000)

        Truth.assertThat(presetTimerList).contains(presetTimer)
    }

    // cancel delete
    @Test
    fun `cancel delete timer, recovery deleted timer`(){
        val timer1 = Timer("timer1")
        timerViewModel.deleteTimer(timer1)
        timerViewModel.restoreTimerAndPresetTimers(timer1)
        val timerList = timerViewModel.timerItems.getOrAwaitValue()
        Truth.assertThat(timerList.contains(timer1)).isTrue()
    }

    @Test
    fun `cancel delete timer, recovery deleted preset timers`() {
        val timer1 = Timer("timer1")
        timerViewModel.getTemporalNotificationTime(600000)
        timerViewModel.insertPresetTimer("preset1", 9000000)

        timerViewModel.deleteTimer(timer1)
        timerViewModel.getCurrentTimerAndPresetTimerList("timer1")
        val presetTimerList = timerViewModel.presetTimerList.getOrAwaitValue()
        val presetTimer = PresetTimer("timer1", "preset1", 1, 9000000, 600000)

        Truth.assertThat(presetTimerList.contains(presetTimer)).isTrue()
    }

    // PresetTimerListFragment
    // update Timer
    @Test
    fun `update timer contains timerName, return success`(){
        timerViewModel.getTemporalNotificationTime(600000)
        timerViewModel.insertPresetTimer("preset1", 9000000)

        timerViewModel.updateTimerNameAndSetting("timer", NotificationType.ALARM)
        val value = timerViewModel.updateTimerStatus.getOrAwaitValue()

        Truth.assertThat(value.getContentIfNotHandled()?.status).isEqualTo(Status.SUCCESS)
    }

    @Test
    fun `update timer contains timerName, accompanied by updating presetTimer`(){
        timerViewModel.getTemporalNotificationTime(600000)
        timerViewModel.insertPresetTimer("preset1", 9000000)

        timerViewModel.updateTimerNameAndSetting("timer", NotificationType.ALARM)
        val presetTimerList = timerViewModel.presetTimerList.getOrAwaitValue()
        val presetTimer = PresetTimer("timer", "preset1", 1, 9000000, 600000)

        Truth.assertThat(presetTimerList.contains(presetTimer)).isTrue()
    }

    @Test
    fun `update timer except for timerName, return success()`(){
        timerViewModel.updateSettingTimer(NotificationType.ALARM)
        val value = timerViewModel.updateTimerStatus.getOrAwaitValue()
        Truth.assertThat(value.getContentIfNotHandled()?.status).isEqualTo(Status.SUCCESS)
    }

    // drag and drop updatePresetTimer
    @Test
    fun `drag and drop preset timer, preset timer's order change`(){
        timerViewModel.getTemporalNotificationTime(0)
        timerViewModel.insertPresetTimer("preset1", 9000000)
        timerViewModel.insertPresetTimer("preset2", 9000000)
        timerViewModel.insertPresetTimer("preset3", 9000000)
        timerViewModel.changePresetTimerOrder(0,1)
        val presetTimerList = timerViewModel.presetTimerList.getOrAwaitValue()
        val presetTimer1 = PresetTimer("timer1", "preset1", 2, 9000000)
        val presetTimer2 = PresetTimer("timer1", "preset2", 1, 9000000)
        val presetTimer3 = PresetTimer("timer1", "preset3", 3, 9000000)
        Truth.assertThat(presetTimerList).isEqualTo(listOf(presetTimer2, presetTimer1, presetTimer3))
    }

    @Test
    fun `preset timer's order change, update timer`(){
        timerViewModel.getTemporalNotificationTime(0)
        timerViewModel.insertPresetTimer("preset1", 9000000)
        timerViewModel.insertPresetTimer("preset2", 9000000)
        timerViewModel.insertPresetTimer("preset3", 9000000)
        timerViewModel.changePresetTimerOrder(2,1)
        val value = timerViewModel.currentTimer.getOrAwaitValue()
        val detail = "preset1\t150分\t通知: なし\npreset3\t150分\t通知: なし\npreset2\t150分\t通知: なし\n"
        val correct = Timer("timer1", 27000000, ListType.DETAIL_LAYOUT, NotificationType.VIBRATION,
            isDisplay = false, detail)
        Truth.assertThat(value).isEqualTo(correct)
    }

    // swipe delete PresetTimer
    // swipe delete Timer
    @Test
    fun `swipe and delete preset timer, return success`(){
        timerViewModel.getTemporalNotificationTime(0)
        timerViewModel.insertPresetTimer("presetTimer1", 6000000)

        val presetTimer = PresetTimer("timer1", "presetTimer1", 1, 6000000, 0)
        timerViewModel.deletePresetTimerAndUpdateTimer(presetTimer)

        val value = timerViewModel.deletePresetTimerStatus.getOrAwaitValue()
        Truth.assertThat(value.getContentIfNotHandled()?.status).isEqualTo(Status.SUCCESS)
    }

    @Test
    fun `swipe preset timer, delete correct preset timer`(){
        timerViewModel.getTemporalNotificationTime(0)
        timerViewModel.insertPresetTimer("presetTimer1", 6000000)

        val presetTimer = PresetTimer("timer1", "presetTimer1", 1, 6000000, 0)
        timerViewModel.deletePresetTimerAndUpdateTimer(presetTimer)

        val value = timerViewModel.presetTimerList.getOrAwaitValue()
        Truth.assertThat(value).doesNotContain(presetTimer)
    }

    @Test
    fun `swipe and delete preset timer, accompanied by updating timer`(){
        timerViewModel.getTemporalNotificationTime(0)
        timerViewModel.insertPresetTimer("presetTimer1", 6000000)

        val presetTimer = PresetTimer("timer1", "presetTimer1", 1, 6000000, 0)
        timerViewModel.deletePresetTimerAndUpdateTimer(presetTimer)
        val value = timerViewModel.currentTimer.getOrAwaitValue()
        val correct = Timer("timer1")
        Truth.assertThat(value).isEqualTo(correct)
    }

    @Test
    fun `cancel delete preset timer, recovery deleted preset timer`(){
        timerViewModel.getTemporalNotificationTime(0)
        timerViewModel.insertPresetTimer("presetTimer1", 6000000)

        val presetTimer = PresetTimer("timer1", "presetTimer1", 1, 6000000, 0)
        timerViewModel.deletePresetTimerAndUpdateTimer(presetTimer)
        timerViewModel.restorePresetTimerAndUpdateTimer(presetTimer)

        val presetTimerList = timerViewModel.presetTimerList.getOrAwaitValue()
        Truth.assertThat(presetTimerList.contains(presetTimer)).isTrue()
    }

    @Test
    fun `cancel delete preset timer, update timer`(){
        timerViewModel.getTemporalNotificationTime(0)
        timerViewModel.insertPresetTimer("presetTimer1", 6000000)

        val presetTimer = PresetTimer("timer1", "presetTimer1", 1, 6000000, 0)
        timerViewModel.deletePresetTimerAndUpdateTimer(presetTimer)
        timerViewModel.restorePresetTimerAndUpdateTimer(presetTimer)

        val value = timerViewModel.currentTimer.getOrAwaitValue()
        val correctTimer = Timer("timer1", 6000000, ListType.SIMPLE_LAYOUT, NotificationType.VIBRATION,
            isDisplay = false, "通知: なし")
        Truth.assertThat(value).isEqualTo(correctTimer)
    }

    // SetTimerFragment
    // insert presetTimer
    @Test
    fun `insert preset timer with Empty name, return error`() {
        timerViewModel.getTemporalNotificationTime(0)
        timerViewModel.insertPresetTimer("", 9000000)
        val value = timerViewModel.nameStatus.getOrAwaitValue()
        Truth.assertThat(value.getContentIfNotHandled()?.status).isEqualTo(Status.ERROR)
    }

    @Test
    fun `insert preset timer with Long name, return error`() {
        timerViewModel.getTemporalNotificationTime(0)
        timerViewModel.insertPresetTimer("VeryVeryVeryVeryLongPresetTimerName", 9000000)
        val value = timerViewModel.nameStatus.getOrAwaitValue()
        Truth.assertThat(value.getContentIfNotHandled()?.status).isEqualTo(Status.ERROR)
    }

    @Test
    fun `insert preset timer with longer notification Time than presetTime, return error`() {
        timerViewModel.getTemporalNotificationTime(6000000)
        timerViewModel.insertPresetTimer("presetTimer", 0)
        val value = timerViewModel.showTimerError.getOrAwaitValue()
        Truth.assertThat(value).isEqualTo("適切にタイマーの設定を行って下さい。")
    }

    @Test
    fun `insert preset timer with zero preset time, return error`() {
        timerViewModel.getTemporalNotificationTime(0)
        timerViewModel.insertPresetTimer("presetTimer", 0)
        val value = timerViewModel.showTimerError.getOrAwaitValue()
        Truth.assertThat(value).isEqualTo("適切にタイマーの設定を行って下さい。")
    }

    @Test
    fun `insert too many preset timer, return error`() {
        timerViewModel.getTemporalNotificationTime(0)
        timerViewModel.insertPresetTimer("preset1", 9000000)
        timerViewModel.insertPresetTimer("preset2", 9000000)
        timerViewModel.insertPresetTimer("preset3", 9000000)
        timerViewModel.insertPresetTimer("preset4", 9000000)
        timerViewModel.insertPresetTimer("preset5", 9000000)
        timerViewModel.insertPresetTimer("preset6", 9000000)
        timerViewModel.insertPresetTimer("preset7", 9000000)
        timerViewModel.insertPresetTimer("preset8", 9000000)
        timerViewModel.insertPresetTimer("preset9", 9000000)
        timerViewModel.insertPresetTimer("preset10", 9000000)
        timerViewModel.navigateToSettingTimer(null, null, null)
        val value = timerViewModel.showTimerError.getOrAwaitValue()
        Truth.assertThat(value).isEqualTo("カスタマイズできるタイマーは${Constants.PRESET_TIMER_NUM}までです。")
    }

    @Test
    fun `insert preset timer with valid input, return success`() {
        timerViewModel.getTemporalNotificationTime(0)
        timerViewModel.insertPresetTimer("presetTimer1", 9000000)
        timerViewModel.getTemporalNotificationTime(300000)
        timerViewModel.insertPresetTimer("presetTimer2", 3600000)
        timerViewModel.getTemporalNotificationTime(0)
        timerViewModel.insertPresetTimer("presetTimer", 9000000)
        val value = timerViewModel.insertAndUpdatePresetTimerStatus.getOrAwaitValue()
        Truth.assertThat(value.getContentIfNotHandled()?.status).isEqualTo(Status.SUCCESS)
    }

    @Test
    fun `insert preset timer with valid input, insert correct preset timers`() {
        timerViewModel.getTemporalNotificationTime(0)
        timerViewModel.insertPresetTimer("presetTimer1", 9000000)
        timerViewModel.getTemporalNotificationTime(300000)
        timerViewModel.insertPresetTimer("presetTimer2", 3600000)
        val presetTimer1 = PresetTimer("timer1", "presetTimer1", 1, 9000000, 0)
        val presetTimer2 = PresetTimer("timer1", "presetTimer2", 2, 3600000, 300000)
        val value = timerViewModel.presetTimerList.getOrAwaitValue()
        Truth.assertThat(value).isEqualTo(listOf(presetTimer1, presetTimer2))
    }

    @Test
    fun `insert preset timer with valid input, accompanied by updating timer`(){
        timerViewModel.getTemporalNotificationTime(0)
        timerViewModel.insertPresetTimer("presetTimer1", 9000000)
        timerViewModel.getTemporalNotificationTime(300000)
        timerViewModel.insertPresetTimer("presetTimer2", 3600000)
        val value = timerViewModel.currentTimer.getOrAwaitValue()
        val detail = "presetTimer1\t150分\t通知: なし\n" +
                "presetTimer2\t60分\t通知: 5分前\n"
        val correct = Timer("timer1", 12600000, ListType.DETAIL_LAYOUT, NotificationType.VIBRATION,
            isDisplay = false, detail)
        Truth.assertThat(value).isEqualTo(correct)
    }

    // update presetTimer
    @Test
    fun `update preset timer contains preset timerName, return success`(){
        timerViewModel.getTemporalNotificationTime(0)
        timerViewModel.insertPresetTimer("presetTimer1", 9000000)
        timerViewModel.getTemporalTime("timer1", "presetTimer1", 1)
        timerViewModel.updatePresetTimer("presetTimer",9000000)
        val value = timerViewModel.insertAndUpdatePresetTimerStatus.getOrAwaitValue()
        Truth.assertThat(value.getContentIfNotHandled()?.status).isEqualTo(Status.SUCCESS)
    }

    @Test
    fun `update preset timer contains timerName, accompanied by updating timer`(){
        timerViewModel.getTemporalNotificationTime(0)
        timerViewModel.insertPresetTimer("presetTimer1", 9000000)
        timerViewModel.insertPresetTimer("presetTimer2", 9000000)
        timerViewModel.getTemporalTime("timer1", "presetTimer1", 1)
        timerViewModel.updatePresetTimer("presetTimer",9000000)
        val detail = "presetTimer\t150分\t通知: なし\npresetTimer2\t150分\t通知: なし\n"
        val correctTimer = Timer("timer1", 18000000, ListType.DETAIL_LAYOUT, NotificationType.VIBRATION,
            isDisplay = false, detail)
        val value = timerViewModel.currentTimer.getOrAwaitValue()
        Truth.assertThat(value).isEqualTo(correctTimer)
    }

    @Test
    fun `update preset timer except for timerName, return success`(){
        timerViewModel.getTemporalNotificationTime(0)
        timerViewModel.insertPresetTimer("presetTimer1", 9000000)
        timerViewModel.getTemporalTime("timer1", "presetTimer1", 1)
        timerViewModel.getTemporalNotificationTime(600000)
        timerViewModel.updatePresetTimer("presetTimer1",6000000)
        val value = timerViewModel.insertAndUpdatePresetTimerStatus.getOrAwaitValue()
        Truth.assertThat(value.getContentIfNotHandled()?.status).isEqualTo(Status.SUCCESS)
    }

    @Test
    fun `update preset timer except for timerName, accompanied by updating timer`(){
        timerViewModel.getTemporalNotificationTime(0)
        timerViewModel.insertPresetTimer("presetTimer1", 6000000)
        timerViewModel.insertPresetTimer("presetTimer2", 9000000)
        timerViewModel.getTemporalTime("timer1", "presetTimer1", 1)
        timerViewModel.updatePresetTimer("presetTimer1",9000000)
        val detail = "presetTimer1\t150分\t通知: なし\npresetTimer2\t150分\t通知: なし\n"
        val correctTimer = Timer("timer1", 18000000, ListType.DETAIL_LAYOUT, NotificationType.VIBRATION,
            isDisplay = false, detail)
        val value = timerViewModel.currentTimer.getOrAwaitValue()
        Truth.assertThat(value).isEqualTo(correctTimer)
    }

    // delete Timer List
    @Test
    fun `delete timer list, return success`(){
        val timer1 = Timer("timer1")
        val timer2 = Timer("timer2")
        val deleteList = listOf(timer1, timer2)
        timerViewModel.deleteTimerListAndPresetTimerList(deleteList)
        val value = timerViewModel.deleteTimerItemStatus.getOrAwaitValue()
        Truth.assertThat(value.getContentIfNotHandled()?.status).isEqualTo(Status.SUCCESS)
    }

    // delete presetTimerList
    @Test
    fun `delete preset timer list, return success`(){
        timerViewModel.getTemporalNotificationTime(600000)
        timerViewModel.insertPresetTimer("presetTimer1", 9000000)
        timerViewModel.insertPresetTimer("presetTimer2", 9000000)

        val presetTimer1 = PresetTimer("timer1", "presetTimer1", 1, 9000000, 600000)
        val presetTimer2 = PresetTimer("timer1", "presetTimer2", 2, 9000000, 600000)
        val deleteList = listOf(presetTimer1, presetTimer2)
        timerViewModel.deletePresetTimerList(deleteList)
        val value = timerViewModel.deletePresetTimerStatus.getOrAwaitValue()
        Truth.assertThat(value.getContentIfNotHandled()?.status).isEqualTo(Status.SUCCESS)
    }
}