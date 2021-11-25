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
import java.math.BigDecimal
import kotlin.concurrent.timer

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

    // タイマー名の登録と変更の際に使用
    // 以下はエラー表示(このfunctionはnameのみの入力)
    // 空の入力
    // 15文字以内
    // 既に同じタイマーの名前が登録されている
    // タイマー数が15個登録されている
    @Test
    fun `insert timer item with empty name, return error`() {
        timerViewModel.checkInputTimerName("")
        val value = timerViewModel.timerNameStatus.getOrAwaitValue()
        Truth.assertThat(value.getContentIfNotHandled()?.status).isEqualTo(Status.ERROR)
    }

    @Test
    fun `insert timer item with too long name, return error`() {
        timerViewModel.checkInputTimerName("VeryVeryVeryLongNameTimer")
        val value = timerViewModel.timerNameStatus.getOrAwaitValue()
        Truth.assertThat(value.getContentIfNotHandled()?.status).isEqualTo(Status.ERROR)
    }

    @Test
    fun `insert timer item with registered name, return error`() {
        timerViewModel.checkInputTimerName("timer1")
        val value = timerViewModel.timerNameStatus.getOrAwaitValue()
        Truth.assertThat(value.getContentIfNotHandled()?.status).isEqualTo(Status.ERROR)
    }

    @Test
    fun `insert many timers, return error()`(){
        timerViewModel.insertTimer("timer3")
        timerViewModel.insertTimer("timer4")
        timerViewModel.insertTimer("timer5")
        timerViewModel.insertTimer("timer6")
        timerViewModel.insertTimer("timer7")
        timerViewModel.insertTimer("timer8")
        timerViewModel.insertTimer("timer9")
        timerViewModel.insertTimer("timer10")
        timerViewModel.insertTimer("timer11")
        timerViewModel.insertTimer("timer12")
        timerViewModel.insertTimer("timer13")
        timerViewModel.insertTimer("timer14")
        timerViewModel.insertTimer("timer15")
        timerViewModel.checkInputTimerName("timer16")
        val value = timerViewModel.timerNameStatus.getOrAwaitValue()
        Truth.assertThat(value.getContentIfNotHandled()?.status).isEqualTo(Status.ERROR)
    }

    @Test
    fun `insert timer item with valid input, return success`() {
        timerViewModel.checkInputTimerName("timer")
        val value = timerViewModel.timerNameStatus.getOrAwaitValue()
        Truth.assertThat(value.getContentIfNotHandled()?.status).isEqualTo(Status.SUCCESS)
    }

    @Test
    fun `update timerName accompanied by updating PresetTimer`(){
        timerViewModel.getTemporalNotificationTime(600000)
        timerViewModel.insertPresetTimer("preset1", 9000000)

        timerViewModel.updateTimerNameAndSetting("timer", NotificationType.ALARM, true)
        val value = timerViewModel.updateTimerStatus.getOrAwaitValue()
        val presetTimerList = timerViewModel.presetTimerList.getOrAwaitValue()
        val presetTimer = PresetTimer("timer", "preset1", 1, 9000000, 600000)

        Truth.assertThat(value.getContentIfNotHandled()?.status).isEqualTo(Status.SUCCESS)
        Truth.assertThat(presetTimerList.contains(presetTimer)).isTrue()
    }

    @Test
    fun `update timerSetting, return success()`(){
        timerViewModel.updateSettingTimer(NotificationType.ALARM, false)
        val value = timerViewModel.updateTimerStatus.getOrAwaitValue()
        Truth.assertThat(value.getContentIfNotHandled()?.status).isEqualTo(Status.SUCCESS)
    }

    // スワイプとリスト削除両方をテスト
    // タイマーを削除できるか
    // プリセットタイマーも削除されるか
    @Test
    fun `swipe and delete timer item, return success`(){
        val timer1 = Timer("timer1")
        timerViewModel.deleteTimer(timer1)
        val allTimerList = timerViewModel.timerItems.getOrAwaitValue()
        val value = timerViewModel.deleteTimerItemStatus.getOrAwaitValue()
        Truth.assertThat(allTimerList.contains(timer1)).isFalse()
        Truth.assertThat(value.getContentIfNotHandled()?.status).isEqualTo(Status.SUCCESS)
    }

    @Test
    fun `swipe and delete timer item accompanied by deletion of presetimers`(){
        val timer1 = Timer("timer1")
        timerViewModel.getTemporalNotificationTime(600000)
        timerViewModel.insertPresetTimer("preset1", 9000000)

        timerViewModel.deleteTimer(timer1)
        timerViewModel.getCurrentTimerAndPresetTimerList("timer1")
        val presetTimerList = timerViewModel.presetTimerList.getOrAwaitValue()
        val value = timerViewModel.deleteTimerItemStatus.getOrAwaitValue()
        val presetTimer = PresetTimer("timer1", "preset1", 1, 9000000, 600000)

        Truth.assertThat(presetTimerList).contains(presetTimer)
        Truth.assertThat(value.getContentIfNotHandled()?.status).isEqualTo(Status.SUCCESS)
    }

    @Test
    fun `delete timer list, return success`(){
        val timer1 = Timer("timer1")
        val timer2 = Timer("timer2")
        val deleteList = listOf(timer1, timer2)
        timerViewModel.deleteTimerList(deleteList)
        val value = timerViewModel.deleteTimerItemStatus.getOrAwaitValue()
        Truth.assertThat(value.getContentIfNotHandled()?.status).isEqualTo(Status.SUCCESS)
    }

    @Test
    fun `delete timer list accompanied by deletion preset timers`(){
        timerViewModel.getTemporalNotificationTime(600000)
        timerViewModel.insertPresetTimer("preset1", 9000000)

        val timer1 = Timer("timer1")
        val timer2 = Timer("timer2")
        val deleteList = listOf(timer1, timer2)

        timerViewModel.deleteTimerList(deleteList)

        timerViewModel.getCurrentTimerAndPresetTimerList("timer1")
        val presetTimerList = timerViewModel.presetTimerList.getOrAwaitValue()
        val value = timerViewModel.deleteTimerItemStatus.getOrAwaitValue()
        val presetTimer = PresetTimer("timer1", "preset1", 1, 9000000, 600000)
        Truth.assertThat(presetTimerList).contains(presetTimer)
        Truth.assertThat(value.getContentIfNotHandled()?.status).isEqualTo(Status.SUCCESS)
    }

    // スワイプして削除したタイマーの復元
    // プリセットタイマーの復元
    @Test
    fun `recovery deleted timer`(){
        val timer1 = Timer("timer1")
        timerViewModel.deleteTimer(timer1)
        timerViewModel.restoreTimerAndRelatedPresetTimers(timer1)
        val timerList = timerViewModel.timerItems.getOrAwaitValue()
        Truth.assertThat(timerList.contains(timer1)).isTrue()
    }

    @Test
    fun `recovery deleted timer accompanied by preset timers`() {
        val timer1 = Timer("timer1")
        timerViewModel.getTemporalNotificationTime(600000)
        timerViewModel.insertPresetTimer("preset1", 9000000)

        timerViewModel.deleteTimer(timer1)
        timerViewModel.getCurrentTimerAndPresetTimerList("timer1")
        val presetTimerList = timerViewModel.presetTimerList.getOrAwaitValue()
        val presetTimer = PresetTimer("timer1", "preset1", 1, 9000000, 600000)

        Truth.assertThat(presetTimerList.contains(presetTimer)).isTrue()
    }

    @Test
    fun `insert preset timer item with Empty name, return error`() {
        timerViewModel.getTemporalNotificationTime(0)
        timerViewModel.insertPresetTimer("", 9000000)
        val value = timerViewModel.timerNameStatus.getOrAwaitValue()
        Truth.assertThat(value.getContentIfNotHandled()?.status).isEqualTo(Status.ERROR)
    }

    @Test
    fun `insert preset timer item with Long name, return error`() {
        timerViewModel.getTemporalNotificationTime(0)
        timerViewModel.insertPresetTimer("presetTimerPresetTimer", 9000000)
        val value = timerViewModel.timerNameStatus.getOrAwaitValue()
        Truth.assertThat(value.getContentIfNotHandled()?.status).isEqualTo(Status.ERROR)
    }

    @Test
    fun `insert preset timer item with longer notification Time than presetTime, return error`() {
        timerViewModel.getTemporalNotificationTime(0)
        timerViewModel.insertPresetTimer("presetTimer", 0)
        val value = timerViewModel.showTimerError.getOrAwaitValue()
        Truth.assertThat(value).isEqualTo("適切にタイマーの設定を行って下さい。")
    }

    @Test
    fun `insert too many preset timer item, return error`() {
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
        timerViewModel.insertPresetTimer("preset11", 9000000)
        timerViewModel.insertPresetTimer("preset12", 9000000)
        timerViewModel.navigateToSettingTimer(null, null, null)
        val value = timerViewModel.showTimerError.getOrAwaitValue()
        Truth.assertThat(value).isEqualTo("カスタマイズできるタイマーは${Constants.PRESET_TIMER_NUM}までです。")
    }

    @Test
    fun `insert preset timer item with valid input, return success`() {
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
    fun `insert multiple preset timer items with valid input, accompanied by timer change`(){
        timerViewModel.getTemporalNotificationTime(0)
        timerViewModel.insertPresetTimer("presetTimer1", 9000000)
        timerViewModel.getTemporalNotificationTime(300000)
        timerViewModel.insertPresetTimer("presetTimer2", 3600000)
        val value = timerViewModel.currentTimer.getOrAwaitValue()
        val detail = "presetTimer1\t150分\t通知: なし\n" +
                "presetTimer2\t60分\t通知: 5分前\n"
        val correct = Timer("timer1", 12600000, ListType.DETAIL_LAYOUT, NotificationType.VIBRATION,
            isDisplay = true, detail)
        Truth.assertThat(value).isEqualTo(correct)
    }

    @Test
    fun `update presetTimer item with Empty name, return error`() {
        timerViewModel.getTemporalNotificationTime(0)
        timerViewModel.insertPresetTimer("presetTimer1", 9000000)
        timerViewModel.getTemporalTime("timer1", "presetTimer1", 1)
        timerViewModel.updatePresetTimer("", 9000000)
        val value = timerViewModel.timerNameStatus.getOrAwaitValue()
        Truth.assertThat(value.getContentIfNotHandled()?.status).isEqualTo(Status.ERROR)
    }

    @Test
    fun `update presetTimer item with Long name, return error`() {
        timerViewModel.getTemporalNotificationTime(0)
        timerViewModel.insertPresetTimer("presetTimer1", 9000000)
        timerViewModel.getTemporalTime("timer1", "presetTimer1", 1)
        timerViewModel.updatePresetTimer("presetTimerPresetTimer", 9000000)
        val value = timerViewModel.timerNameStatus.getOrAwaitValue()
        Truth.assertThat(value.getContentIfNotHandled()?.status).isEqualTo(Status.ERROR)
    }

    @Test
    fun `update presetTimer item with longer notification Time than presetTime, return error`() {
        timerViewModel.getTemporalNotificationTime(0)
        timerViewModel.insertPresetTimer("presetTimer1", 9000000)
        timerViewModel.getTemporalTime("timer1", "presetTimer1", 1)
        timerViewModel.getTemporalNotificationTime(9000000)
        timerViewModel.updatePresetTimer("presetTimer1", 0)
        val value = timerViewModel.showTimerError.getOrAwaitValue()
        Truth.assertThat(value).isEqualTo("適切にタイマーの設定を行って下さい。")
    }

    @Test
    fun `update valid presetName, return success`() {
        timerViewModel.getTemporalNotificationTime(0)
        timerViewModel.insertPresetTimer("presetTimer1", 9000000)
        timerViewModel.getTemporalTime("timer1", "presetTimer1", 1)
        timerViewModel.updatePresetTimer("presetTimer",9000000)
        val value = timerViewModel.timerNameStatus.getOrAwaitValue()
        Truth.assertThat(value.getContentIfNotHandled()?.status).isEqualTo(Status.SUCCESS)
    }

    @Test
    fun `update valid presetName accompanied by updating Timer`(){
        timerViewModel.getTemporalNotificationTime(0)
        timerViewModel.insertPresetTimer("presetTimer1", 9000000)
        timerViewModel.insertPresetTimer("presetTimer2", 9000000)
        timerViewModel.getTemporalTime("timer1", "presetTimer1", 1)
        timerViewModel.updatePresetTimer("presetTimer",9000000)
        val updatedTimer = timerViewModel.currentTimer.getOrAwaitValue()
        val updatedPresetTimer = timerViewModel.presetTimerList.getOrAwaitValue()
        val detail = "presetTimer\t150分\t通知: なし\n" +
                "presetTimer2\t150分\t通知: なし\n"
        val timer = Timer("timer1", 18000000, ListType.DETAIL_LAYOUT, NotificationType.VIBRATION,
            isDisplay = true, detail)
        val presetTimer = PresetTimer("timer1", "presetTimer", 1, 900000, 0)
        Truth.assertThat(updatedTimer).isEqualTo(timer)
        Truth.assertThat(updatedPresetTimer.contains(presetTimer))
    }

    @Test
    fun `update valid setting accompanied by updating Timer`(){
        timerViewModel.getTemporalNotificationTime(600000)
        timerViewModel.insertPresetTimer("presetTimer1", 9000000)
        timerViewModel.insertPresetTimer("presetTimer2", 9000000)
        timerViewModel.getTemporalTime("timer1", "presetTimer1", 1)
        timerViewModel.getTemporalNotificationTime(0)
        timerViewModel.updatePresetTimer("presetTimer",6000000)
        val updatedTimer = timerViewModel.currentTimer.getOrAwaitValue()
        val updatedPresetTimer = timerViewModel.presetTimerList.getOrAwaitValue()
        val detail = "presetTimer\t100分\t通知: なし\npresetTimer2\t150分\t通知: 10分前\n"
        val timer = Timer("timer1", 15000000, ListType.DETAIL_LAYOUT, NotificationType.VIBRATION,
            isDisplay = true, detail)
        val presetTimer = PresetTimer("timer1", "presetTimer", 1, 600000, 0)
        Truth.assertThat(updatedTimer).isEqualTo(timer)
        Truth.assertThat(updatedPresetTimer.contains(presetTimer))
    }


    @Test
    fun `swipe and delete presetTimer, accompanied by updating timer`() {
        timerViewModel.getTemporalNotificationTime(0)
        timerViewModel.insertPresetTimer("presetTimer1", 6000000)

        val presetTimer = PresetTimer("timer1", "presetTimer1", 1, 6000000, 0)
        timerViewModel.deletePresetTimerAndUpdateRelatedTimer(presetTimer)

        val value = timerViewModel.deletePresetTimerStatus.getOrAwaitValue()
        val currentTimer = timerViewModel.currentTimer.getOrAwaitValue()
        val correctTimer = Timer("timer1", 0, ListType.SIMPLE_LAYOUT, NotificationType.VIBRATION,
            isDisplay = true,  "no presetTimer")
        Truth.assertThat(value.getContentIfNotHandled()?.status).isEqualTo(Status.SUCCESS)
        Truth.assertThat(currentTimer).isEqualTo(correctTimer)
    }

    @Test
    fun `delete presetTimer list, accompanied by updating timer`(){
        timerViewModel.getTemporalNotificationTime(600000)
        timerViewModel.insertPresetTimer("presetTimer1", 9000000)
        timerViewModel.insertPresetTimer("presetTimer2", 9000000)

        val presetTimer1 = PresetTimer("timer1", "presetTimer1", 1, 9000000, 600000)
        val presetTimer2 = PresetTimer("timer1", "presetTimer2", 2, 9000000, 600000)
        val deleteList = listOf(presetTimer1, presetTimer2)
        timerViewModel.deletePresetTimerList(deleteList)
        val value = timerViewModel.deletePresetTimerStatus.getOrAwaitValue()
        val currentTimer = timerViewModel.currentTimer.getOrAwaitValue()
        val correctTimer = Timer("timer1", 0, ListType.SIMPLE_LAYOUT, NotificationType.VIBRATION,
            isDisplay = true,  "no presetTimer")
        Truth.assertThat(value.getContentIfNotHandled()?.status).isEqualTo(Status.SUCCESS)
        Truth.assertThat(currentTimer).isEqualTo(correctTimer)
    }

    // スワイプして削除したタイマーの復元
    // プリセットタイマーの復元
    @Test
    fun `recovery deleted presetTimer`(){
        timerViewModel.getTemporalNotificationTime(0)
        timerViewModel.insertPresetTimer("presetTimer1", 6000000)

        val presetTimer = PresetTimer("timer1", "presetTimer1", 1, 6000000, 0)
        timerViewModel.deletePresetTimerAndUpdateRelatedTimer(presetTimer)
        timerViewModel.restorePresetTimerAndUpdateRelatedTimer(presetTimer)

        val timer = timerViewModel.currentTimer.getOrAwaitValue()
        val presetTimerList = timerViewModel.presetTimerList.getOrAwaitValue()
        val correctTimer = Timer("timer1", 6000000, ListType.SIMPLE_LAYOUT, NotificationType.VIBRATION,
            isDisplay = true, "通知: なし")
        Truth.assertThat(timer).isEqualTo(correctTimer)
        Truth.assertThat(presetTimerList.contains(presetTimer)).isTrue()
    }


}