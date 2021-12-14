package com.example.timerapp

import android.view.InputDevice
import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.GeneralClickAction
import androidx.test.espresso.action.GeneralLocation
import androidx.test.espresso.action.Press
import androidx.test.espresso.action.Tap
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.example.timerapp.database.ListType
import com.example.timerapp.database.NotificationType
import com.example.timerapp.database.PresetTimer
import com.example.timerapp.database.Timer
import com.example.timerapp.others.Constants
import com.example.timerapp.repository.TimerRepository
import com.example.timerapp.ui.MainActivity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class MainActivityTest {

    private lateinit var repository: TimerRepository

    private val dataBindingIdlingResource = DataBindingIdlingResource()

    @get:Rule
    var animationTestRule = AnimationTestRule()

    @Before
    fun init() {
        repository = ServiceLocator.provideTimerRepository(getApplicationContext())
    }

    @After
    fun reset() {
        ServiceLocator.resetRepository()
    }

    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }

    @Test
    fun addFirstTimer_onceError_modifiedAndReInsert() = runBlocking {
        val activityScenario = ActivityScenario.launch(MainActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // TimerListFragment
        // 初期メッセージの表示
        onView(withId(R.id.initial_timer_message)).check(matches(isDisplayed()))
        // dialogに新しいタイマー名を入力
        // タイマーを新規作成、タイマー名のエラーを生じさせる
        onView(withId(R.id.add_list)).perform(click())
        onView(withId(R.id.et_timer_name)).perform(click()).perform(replaceText("LongLongLongLongLongTimerName"), closeSoftKeyboard())
        onView(withText("保存")).perform(click())
        onView(withText("タイマー名は${Constants.MAX_NAME_LENGTH}文字までです。"))
        // 正確なタイマー名を入力
        onView(withId(R.id.et_timer_name)).perform(click()).perform(replaceText("newTimer"), closeSoftKeyboard())
        onView(withText("保存")).perform(click())
        // PresetTimerFragment
        // 保存したタイマー名が表示されている事、初期メッセージが表示されていることを確認
        onView(withId(R.id.et_timer_name)).check(matches(isDisplayed()))
        onView(withId(R.id.et_timer_name)).check(matches(withText("newTimer")))
        onView(withId(R.id.initial_message)).check(matches(isDisplayed()))
        // 保存した内容がホームフラグメント(TimerListFragment)に反映されているか確認
        onView(withId(R.id.back_btn)).perform(click())
        onView(withId(R.id.simple_timer_name)).check(matches(isDisplayed()))
        onView(withId(R.id.simple_timer_name)).check(matches(withText("newTimer")))

        activityScenario.close()
    }

    @Test
    fun insertTimers_SwipeAndDeleteTimer_RecoverTimer() = runBlocking {
        val activityScenario = ActivityScenario.launch(MainActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // insertTimer - timer1
        onView(withId(R.id.add_list)).perform(click())
        onView(withId(R.id.et_timer_name)).perform(click()).perform(replaceText("timer1"), closeSoftKeyboard())
        onView(withText("保存")).perform(click())
        onView(withId(R.id.back_btn)).perform(click())
        // insertTimer - timer2 - preset1 -preset2
        onView(withId(R.id.add_list)).perform(click())
        onView(withId(R.id.et_timer_name)).perform(click()).perform(replaceText("timer2"), closeSoftKeyboard())
        onView(withText("保存")).perform(click())
        // presetTimerを保存
        onView(withId(R.id.add_list)).perform(click())
        onView(withId(R.id.et_presetName)).perform(click()).perform(replaceText("preset1"), closeSoftKeyboard())
        onView(withId(R.id.numberPicker1)).perform(clickBottomCentre)
        onView(withId(R.id.save_button)).perform(click())
        onView(withId(R.id.add_list)).perform(click())
        onView(withId(R.id.et_presetName)).perform(click()).perform(replaceText("preset2"), closeSoftKeyboard())
        onView(withId(R.id.numberPicker1)).perform(clickBottomCentre)
        onView(withId(R.id.numberPicker2)).perform(clickBottomCentre)
        onView(withId(R.id.numberPicker2)).perform(clickBottomCentre)
        onView(withId(R.id.save_button)).perform(click())

        // PresetTimerListFragment presetTimerが登録されたかどうか
        onView(withText("preset1")).check(matches(isDisplayed()))
        onView(withText("preset2")).check(matches(isDisplayed()))
        onView(withText("100分")).check(matches(isDisplayed()))
        onView(withText("120分")).check(matches(isDisplayed()))
        onView(withId(R.id.back_btn)).perform(click()) // 戻るボタン

        // TimerListFragment
        onView(withText("timer1")).check(matches(isDisplayed()))
        onView(withText("timer2")).check(matches(isDisplayed()))
        onView(withText("計 220分")).check(matches(isDisplayed()))
        onView(withId(R.id.detail_title)).perform(click())
        onView(withId(R.id.detail_detail)).check(matches(isDisplayed()))
        onView(withId(R.id.detail_detail)).check(matches(withText("preset1\t100分\t通知: なし\n" +
                "preset2\t120分\t通知: なし\n")))
        onView(withId(R.id.detail_title)).perform(click())

        // swipe and delete timer2
        onView(withId(R.id.timer_list)).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                1, swipeRight()
            )
        )
        // タイマー取り消し完了の通知
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(isDisplayed()))
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(withText("timer2を削除しました。")))
        onView(withText("timer2")).check(doesNotExist())
        onView(withText("取り消し")).perform(click()) // recover
        Thread.sleep(3000)
        // 削除したタイマーが復活しているか確認
        onView(withText("timer2")).check(matches(isDisplayed()))
        onView(withId(R.id.detail_title)).perform(click())
        onView(withId(R.id.detail_detail)).check(matches(isDisplayed()))
        onView(withId(R.id.detail_detail)).check(matches(withText("preset1\t100分\t通知: なし\npreset2\t120分\t通知: なし\n")))

        activityScenario.close()
    }

    @Test
    fun insertTimer_deleteTimer_AccompaniedByDeletionPresetTimers() = runBlocking {
        val activityScenario = ActivityScenario.launch(MainActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // insertTimer - timer1 - preset1 - preset2
        onView(withId(R.id.add_list)).perform(click())
        onView(withId(R.id.et_timer_name)).perform(click()).perform(replaceText("timer1"), closeSoftKeyboard())
        onView(withText("保存")).perform(click())

        // presetTimerを保存
        onView(withId(R.id.add_list)).perform(click())
        onView(withId(R.id.et_presetName)).perform(click()).perform(replaceText("preset1"), closeSoftKeyboard())
        onView(withId(R.id.numberPicker1)).perform(clickBottomCentre)
        onView(withId(R.id.save_button)).perform(click())
        onView(withId(R.id.add_list)).perform(click())
        onView(withId(R.id.et_presetName)).perform(click()).perform(replaceText("preset2"), closeSoftKeyboard())
        onView(withId(R.id.numberPicker1)).perform(clickBottomCentre)
        onView(withId(R.id.numberPicker2)).perform(clickBottomCentre)
        onView(withId(R.id.numberPicker2)).perform(clickBottomCentre)
        onView(withId(R.id.save_button)).perform(click())

        // PresetTimerListFragment presetTimerが登録されたかどうか
        onView(withText("preset1")).check(matches(isDisplayed()))
        onView(withText("preset2")).check(matches(isDisplayed()))
        onView(withText("100分")).check(matches(isDisplayed()))
        onView(withText("120分")).check(matches(isDisplayed()))
        onView(withId(R.id.back_btn)).perform(click()) // 戻るボタン

        // swipe and delete
        onView(withId(R.id.timer_list)).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                0, swipeRight()
            )
        )

        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(isDisplayed()))
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(withText("timer1を削除しました。")))
        onView(withText("timer1")).check(doesNotExist())

        // 再びtimer1を作成してもpreset1とpreset2は現れない
        onView(withId(R.id.add_list)).perform(click())
        onView(withId(R.id.et_timer_name)).perform(click()).perform(replaceText("timer1"), closeSoftKeyboard())
        onView(withText("保存")).perform(click())

        onView(withId(R.id.et_timer_name)).check(matches(withText("timer1")))
        onView(withText("preset1")).check(doesNotExist())
        onView(withText("preset2")).check(doesNotExist())

        activityScenario.close()
    }

    @Test
    fun deleteTimersInDeleteTimerListFragment() = runBlocking {
        val timer1 = Timer("timer1", 900000, ListType.SIMPLE_LAYOUT, NotificationType.VIBRATION, false,
            "通知: なし")
        val preset1 = PresetTimer("timer1", "preset", 1, 900000)
        val timer2 = Timer("timer2", 1500000, ListType.DETAIL_LAYOUT, NotificationType.VIBRATION, false,
            "preset1\t10分\t通知: なし\npreset2\t15分\t通知: なし\n")
        val preset2 = PresetTimer("timer2", "preset1", 1, 600000)
        val preset3 = PresetTimer("timer2", "preset2", 2, 900000)
        val timer3 = Timer("timer3")
        val timer4 = Timer("timer4")
        val timer5 = Timer("timer5")
        repository.insertTimerAndPresetTimers(timer1, listOf(preset1))
        repository.insertTimerAndPresetTimers(timer2, listOf(preset2,preset3))
        repository.insertTimer(timer3)
        repository.insertTimer(timer4)
        repository.insertTimer(timer5)

        val activityScenario = ActivityScenario.launch(MainActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)
        // DeleteTimerListFragmentへ
        onView(withId(R.id.delete_list)).perform(click())

        // 登録したtimerがあるか確認
        onView(withText("timer1")).check(matches(isDisplayed()))
        onView(withText("timer2")).check(matches(isDisplayed()))
        onView(withText("timer3")).check(matches(isDisplayed()))
        onView(withText("timer4")).check(matches(isDisplayed()))
        onView(withText("timer5")).check(matches(isDisplayed()))
        // tapを繰り返しても正確に選択できるかtimer1,3,5を削除
        onView(withId(R.id.delete_timer_list)).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                0, click()
            )
        )
        onView(withId(R.id.delete_timer_list)).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                1, click()
            )
        )
        onView(withId(R.id.delete_timer_list)).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                2, click()
            )
        )
        onView(withId(R.id.delete_timer_list)).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                1, click()
            )
        )
        onView(withId(R.id.delete_timer_list)).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                4, click()
            )
        )
        onView(withId(R.id.delete_timer_list)).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                4, click()
            )
        )
        onView(withId(R.id.delete_timer_list)).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                4, click()
            )
        )
        onView(withId(R.id.delete_btn)).perform(click())
        // TimerListFragmentで削除したitemがないか確認
        onView(withText("timer1")).check(doesNotExist())
        onView(withText("timer2")).check(matches(isDisplayed()))
        onView(withText("timer3")).check(doesNotExist())
        onView(withText("timer4")).check(matches(isDisplayed()))
        onView(withText("timer5")).check(doesNotExist())

        // DeleteTimerListFragmentでも削除したitemが残っていないか確認
        onView(withId(R.id.delete_list)).perform(click())
        onView(withText("timer1")).check(doesNotExist())
        onView(withText("timer2")).check(matches(isDisplayed()))
        onView(withText("timer3")).check(doesNotExist())
        onView(withText("timer4")).check(matches(isDisplayed()))
        onView(withText("timer5")).check(doesNotExist())

        activityScenario.close()
    }

    @Test
    fun cancelDeleteTimers_resetDeleteTimerList() = runBlocking {
        val timer1 = Timer("timer1", 900000, ListType.SIMPLE_LAYOUT, NotificationType.VIBRATION, false,
            "通知: なし")
        val preset1 = PresetTimer("timer1", "preset", 1, 900000)
        val timer2 = Timer("timer2", 1500000, ListType.DETAIL_LAYOUT, NotificationType.VIBRATION, false,
            "preset1\t10分\t通知: なし\npreset2\t15分\t通知: なし\n")
        val preset2 = PresetTimer("timer2", "preset1", 1, 600000)
        val preset3 = PresetTimer("timer2", "preset2", 2, 900000)
        val timer3 = Timer("timer3")
        val timer4 = Timer("timer4")
        val timer5 = Timer("timer5")
        repository.insertTimerAndPresetTimers(timer1, listOf(preset1))
        repository.insertTimerAndPresetTimers(timer2, listOf(preset2,preset3))
        repository.insertTimer(timer3)
        repository.insertTimer(timer4)
        repository.insertTimer(timer5)

        val activityScenario = ActivityScenario.launch(MainActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)
        // DeleteTimerListFragmentへ
        onView(withId(R.id.delete_list)).perform(click())

        // 登録したタイマーがあるかどうかを確認
        onView(withText("timer1")).check(matches(isDisplayed()))
        onView(withText("timer2")).check(matches(isDisplayed()))
        onView(withText("timer3")).check(matches(isDisplayed()))
        onView(withText("timer4")).check(matches(isDisplayed()))
        onView(withText("timer5")).check(matches(isDisplayed()))
        // tapを繰り返しても正確に選択できるかtimer1,2,3を選択
        onView(withId(R.id.delete_timer_list)).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                0, click()
            )
        )
        onView(withId(R.id.delete_timer_list)).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                1, click()
            )
        )
        onView(withId(R.id.delete_timer_list)).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                2, click()
            )
        )
        onView(withId(R.id.delete_timer_list)).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                1, click()
            )
        )
        onView(withId(R.id.delete_timer_list)).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                4, click()
            )
        )
        onView(withId(R.id.delete_timer_list)).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                4, click()
            )
        )
        onView(withId(R.id.delete_timer_list)).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                1, click()
            )
        )
        onView(withId(R.id.cancel_btn)).perform(click())
        // TimerListFragmentではtimerは削除されていない
        onView(withText("timer1")).check(matches(isDisplayed()))
        onView(withText("timer2")).check(matches(isDisplayed()))
        onView(withText("timer3")).check(matches(isDisplayed()))
        onView(withText("timer4")).check(matches(isDisplayed()))
        onView(withText("timer5")).check(matches(isDisplayed()))

        // DeleteTimerListFragmentでもtimerは削除されていない
        onView(withId(R.id.delete_list)).perform(click())
        onView(withText("timer1")).check(matches(isDisplayed()))
        onView(withText("timer2")).check(matches(isDisplayed()))
        onView(withText("timer3")).check(matches(isDisplayed()))
        onView(withText("timer4")).check(matches(isDisplayed()))
        onView(withText("timer5")).check(matches(isDisplayed()))

        activityScenario.close()
    }

    @Test
    fun insertTimerAndPresetTimers_UpdateTimer_onceError_modifiedInput() = runBlocking {
        val activityScenario = ActivityScenario.launch(MainActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // insertTimer - timer1 - preset1 -preset2
        onView(withId(R.id.add_list)).perform(click())
        onView(withId(R.id.et_timer_name)).perform(click()).perform(replaceText("timer1"), closeSoftKeyboard())
        onView(withText("保存")).perform(click())
        // presetTimerを保存
        onView(withId(R.id.add_list)).perform(click())
        onView(withId(R.id.et_presetName)).perform(click()).perform(replaceText("preset1"), closeSoftKeyboard())
        onView(withId(R.id.numberPicker1)).perform(clickBottomCentre)
        onView(withId(R.id.save_button)).perform(click())
        onView(withId(R.id.add_list)).perform(click())
        onView(withId(R.id.et_presetName)).perform(click()).perform(replaceText("preset2"), closeSoftKeyboard())
        onView(withId(R.id.numberPicker1)).perform(clickBottomCentre)
        onView(withId(R.id.numberPicker2)).perform(clickBottomCentre)
        onView(withId(R.id.numberPicker2)).perform(clickBottomCentre)
        onView(withId(R.id.save_button)).perform(click())

        // TimerListFragmentにてTimerの値を確認
        onView(withId(R.id.back_btn)).perform(click())
        onView(withText("timer1")).check(matches(isDisplayed()))
        onView(withText("バイブレーション")).check(matches(isDisplayed()))

        onView(withId(R.id.timer_list)).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                0, click()
            )
        )

        // PresetTimerListFragmentにてTimerの初期値を確認
        onView(withId(R.id.et_timer_name)).check(matches(isDisplayed()))
        onView(withId(R.id.et_timer_name)).check(matches(withText("timer1")))
        onView(withText("preset1")).check(matches(isDisplayed()))
        onView(withText("preset2")).check(matches(isDisplayed()))
        onView(withText("100分")).check(matches(isDisplayed()))
        onView(withText("120分")).check(matches(isDisplayed()))
        // 値の変更 タイマー名のエラーになる
        onView(withId(R.id.et_timer_name)).perform(click()).perform(replaceText(""), closeSoftKeyboard())
        onView(withId(R.id.sounds_spinner)).perform(click())
        onView(withText("アラーム")).perform(click())
        onView(withId(R.id.save_update)).perform(click())
        onView(withText("タイマー名が入力されていません。")).check(matches(isDisplayed()))
        // タイマー名を修正しもう一度保存
        onView(withId(R.id.et_timer_name)).perform(click()).perform(replaceText("updateTimer"), closeSoftKeyboard())
        onView(withId(R.id.save_update)).perform(click())
        // 変更の反映
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(isDisplayed()))
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(withText("タイマーの設定を変更しました。")))

        // TimerListFragmentで値が変更されたことを確認
        onView(withId(R.id.back_btn)).perform(click())
        onView(withText("updateTimer")).check(matches(isDisplayed()))
        onView(withText("アラーム")).check(matches(isDisplayed()))
        activityScenario.close()
    }

    @Test
    fun addPresetTimerAndUpdateTimer() = runBlocking {
        val activityScenario = ActivityScenario.launch(MainActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // insert timer
        onView(withId(R.id.add_list)).perform(click())
        onView(withId(R.id.et_timer_name)).perform(click()).perform(replaceText("newTimer"), closeSoftKeyboard())
        onView(withText("保存")).perform(click())
        // PresetTimerListFragment
        onView(withId(R.id.et_timer_name)).check(matches(isDisplayed()))
        onView(withId(R.id.et_timer_name)).check(matches(withText("newTimer")))
        onView(withId(R.id.initial_message)).check(matches(isDisplayed()))
        onView(withId(R.id.add_list)).perform(click())
        // SetTimerFragment
        onView(withId(R.id.et_presetName)).perform(click()).perform(replaceText("newPreset1"))
        onView(withId(R.id.numberPicker1)).perform(clickBottomCentre)
        onView(withId(R.id.numberPicker2)).perform(clickBottomCentre)
        onView(withId(R.id.numberPicker2)).perform(clickBottomCentre) // 120min
        onView(withId(R.id.pre_notification)).perform(click())
        onView(withId(R.id.numberPickerMin)).perform(clickBottomCentre)
        onView(withId(R.id.numberPickerMin)).perform(clickBottomCentre)
        onView(withId(R.id.numberPickerMin)).perform(clickBottomCentre)
        onView(withId(R.id.numberPickerMin)).perform(clickBottomCentre)
        onView(withId(R.id.numberPickerMin)).perform(clickBottomCentre) // 5min
        onView(withText("保存")).perform(click())
        onView(withId(R.id.setting_preset)).check(matches(withText("通知: 5分 前")))
        onView(withId(R.id.save_button)).perform(click())

        // PresetTimerListFragment
        onView(withId(R.id.preset_timer_name)).check(matches(isDisplayed()))
        onView(withText("newPreset1")).check(matches(isDisplayed()))
        onView(withId(R.id.preset_timer_time)).check(matches(isDisplayed()))
        onView(withText("120分")).check(matches(isDisplayed()))
        onView(withId(R.id.preset_timer_prenotification_time)).check(matches(isDisplayed()))
        onView(withText("通知: 5分 前")).check(matches(isDisplayed()))
        // TimerListFragment タイマーが追加されていることを確認
        onView(withId(R.id.back_btn)).perform(click())
        onView(withText("計 120分")).check(matches(isDisplayed()))
        onView(withText("通知: 5分前")).check(matches(isDisplayed()))

        // さらにnewTimerにタイマーを追加する
        onView(withId(R.id.timer_list)).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                0, click()
            )
        )
        onView(withId(R.id.add_list)).perform(click())
        // SetTimerFragment
        onView(withId(R.id.et_presetName)).perform(click()).perform(replaceText("newPreset2"))
        onView(withId(R.id.numberPicker1)).perform(clickBottomCentre) //100min
        onView(withId(R.id.save_button)).perform(click())

        // PresetTimerListFragment
        onView(withText("newPreset2")).check(matches(isDisplayed()))
        onView(withText("100分")).check(matches(isDisplayed()))
        // TimerListFragment DetailLayoutになっていることを確認
        onView(withId(R.id.back_btn)).perform(click())
        onView(withId(R.id.detail_timer_name)).check(matches(isDisplayed()))
        onView(withId(R.id.detail_timer_name)).check(matches(withText("newTimer")))
        onView(withId(R.id.detail_total_time)).check(matches(isDisplayed()))
        onView(withId(R.id.detail_total_time)).check(matches(withText("計 220分")))
        onView(withId(R.id.detail_title)).perform(click())
        onView(withId(R.id.detail_detail)).check(matches(isDisplayed()))
        onView(withId(R.id.detail_detail)).check(matches(withText("newPreset1\t120分\t通知: 5分前\n" +
                "newPreset2\t100分\t通知: なし\n")))

        activityScenario.close()
    }

    @Test
    fun insertTimerAndPresetTimer_UpdatePresetTimerAndTimer() = runBlocking {
        val activityScenario = ActivityScenario.launch(MainActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // insert timer
        onView(withId(R.id.add_list)).perform(click())
        onView(withId(R.id.et_timer_name)).perform(click()).perform(replaceText("newTimer"), closeSoftKeyboard())
        onView(withText("保存")).perform(click())
        // PresetTimerListFragment
        onView(withId(R.id.et_timer_name)).check(matches(isDisplayed()))
        onView(withId(R.id.et_timer_name)).check(matches(withText("newTimer")))
        onView(withId(R.id.initial_message)).check(matches(isDisplayed()))
        onView(withId(R.id.add_list)).perform(click())
        // SetTimerFragment
        onView(withId(R.id.et_presetName)).perform(click()).perform(replaceText("newPreset1"), closeSoftKeyboard())
        onView(withId(R.id.numberPicker1)).perform(clickBottomCentre)
        onView(withId(R.id.numberPicker2)).perform(clickBottomCentre)
        onView(withId(R.id.numberPicker2)).perform(clickBottomCentre) // 120min
        onView(withId(R.id.pre_notification)).perform(click())
        onView(withId(R.id.numberPickerMin)).perform(clickBottomCentre)
        onView(withId(R.id.numberPickerMin)).perform(clickBottomCentre)
        onView(withId(R.id.numberPickerMin)).perform(clickBottomCentre)
        onView(withId(R.id.numberPickerMin)).perform(clickBottomCentre)
        onView(withId(R.id.numberPickerMin)).perform(clickBottomCentre) // 5min
        onView(withText("保存")).perform(click())
        onView(withId(R.id.setting_preset)).check(matches(withText("通知: 5分 前")))
        onView(withId(R.id.save_button)).perform(click())
        onView(withId(R.id.add_list)).perform(click())

        onView(withId(R.id.et_presetName)).perform(click()).perform(replaceText("newPreset2"), closeSoftKeyboard())
        onView(withId(R.id.numberPicker1)).perform(clickBottomCentre)
        onView(withId(R.id.save_button)).perform(click())

        // PresetTimerListFragment
        onView(withText("newPreset1")).check(matches(isDisplayed()))
        onView(withText("newPreset2")).check(matches(isDisplayed()))
        onView(withText("120分")).check(matches(isDisplayed()))
        onView(withText("100分")).check(matches(isDisplayed()))
        onView(withId(R.id.back_btn)).perform(click())
        // TimerListFragmentでtimerの詳細を確認
        onView(withId(R.id.detail_timer_name)).check(matches(isDisplayed()))
        onView(withId(R.id.detail_timer_name)).check(matches(withText("newTimer")))
        onView(withId(R.id.detail_total_time)).check(matches(isDisplayed()))
        onView(withId(R.id.detail_total_time)).check(matches(withText("計 220分")))
        onView(withId(R.id.detail_title)).perform(click())
        onView(withId(R.id.detail_detail)).check(matches(isDisplayed()))
        onView(withId(R.id.detail_detail)).check(matches(withText("newPreset1\t120分\t通知: 5分前\n" +
                "newPreset2\t100分\t通知: なし\n")))

        onView(withText("newTimer")).perform(click())

        onView(withId(R.id.preset_timer_list)).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                0, click()
            )
        )

        // SetTimerFragment
        onView(withId(R.id.et_presetName)).check(matches(isDisplayed()))
        onView(withId(R.id.et_presetName)).check(matches(withText("newPreset1")))
        onView(withParent(withId(R.id.numberPicker1))).check(matches(withText("1")))
        onView(withParent(withId(R.id.numberPicker2))).check(matches(withText("2")))
        onView(withParent(withId(R.id.numberPicker3))).check(matches(withText("0")))
        onView(withParent(withId(R.id.numberPicker4))).check(matches(withText("0")))
        onView(withParent(withId(R.id.numberPicker5))).check(matches(withText("0")))
        onView(withId(R.id.setting_preset)).check(matches(withText("通知: 5分 前")))
        // newPreset1の変更
        onView(withId(R.id.et_presetName)).perform(click()).perform(replaceText("updateTimer"))
        onView(withId(R.id.numberPicker2)).perform(clickBottomCentre)
        onView(withId(R.id.numberPicker2)).perform(clickBottomCentre)
        onView(withId(R.id.pre_notification)).perform(click())
        onView(withId(R.id.numberPickerMin)).perform(clickTopCentre)
        onView(withId(R.id.numberPickerMin)).perform(clickTopCentre)
        onView(withId(R.id.numberPickerMin)).perform(clickTopCentre)
        onView(withId(R.id.numberPickerMin)).perform(clickTopCentre)
        onView(withId(R.id.numberPickerMin)).perform(clickTopCentre)
        onView(withText("保存")).perform(click())
        onView(withId(R.id.setting_preset)).check(matches(withText("")))
        onView(withId(R.id.save_button)).perform(click())

        // PresetTimer
        onView(withText("updateTimer")).check(matches(isDisplayed()))
        onView(withText("140分")).check(matches(isDisplayed()))
        onView(withId(R.id.back_btn)).perform(click())

        // TimerListFragment
        onView(withText("newTimer")).check(matches(isDisplayed()))
        onView(withText("計 240分")).check(matches(isDisplayed()))
        onView(withText("バイブレーション")).check(matches(isDisplayed()))
        onView(withId(R.id.detail_title)).perform(click())
        onView(withId(R.id.detail_detail)).check(matches(isDisplayed()))
        onView(withId(R.id.detail_detail)).check(matches(withText("updateTimer\t140分\t通知: なし\n" +
                "newPreset2\t100分\t通知: なし\n")))

        activityScenario.close()
    }

    @Test
    fun insertTimerAndPresetTimers_SwipeAndDeletePresetTimer_updateTimer() = runBlocking {
        val activityScenario = ActivityScenario.launch(MainActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // insert timer
        onView(withId(R.id.add_list)).perform(click())
        onView(withId(R.id.et_timer_name)).perform(click()).perform(replaceText("newTimer"), closeSoftKeyboard())
        onView(withText("保存")).perform(click())
        // PresetTimerListFragment
        onView(withId(R.id.et_timer_name)).check(matches(isDisplayed()))
        onView(withId(R.id.et_timer_name)).check(matches(withText("newTimer")))
        onView(withId(R.id.initial_message)).check(matches(isDisplayed()))
        onView(withId(R.id.add_list)).perform(click())
        // SetTimerFragment
        onView(withId(R.id.et_presetName)).perform(click()).perform(replaceText("newPreset1"), closeSoftKeyboard())
        onView(withId(R.id.numberPicker1)).perform(clickBottomCentre)
        onView(withId(R.id.numberPicker2)).perform(clickBottomCentre)
        onView(withId(R.id.numberPicker2)).perform(clickBottomCentre) // 120min
        onView(withId(R.id.pre_notification)).perform(click())
        onView(withId(R.id.numberPickerMin)).perform(clickBottomCentre)
        onView(withId(R.id.numberPickerMin)).perform(clickBottomCentre)
        onView(withId(R.id.numberPickerMin)).perform(clickBottomCentre)
        onView(withId(R.id.numberPickerMin)).perform(clickBottomCentre)
        onView(withId(R.id.numberPickerMin)).perform(clickBottomCentre) // 5min
        onView(withText("保存")).perform(click())
        onView(withId(R.id.setting_preset)).check(matches(withText("通知: 5分 前")))
        onView(withId(R.id.save_button)).perform(click())
        onView(withId(R.id.add_list)).perform(click())

        onView(withId(R.id.et_presetName)).perform(click()).perform(replaceText("newPreset2"), closeSoftKeyboard())
        onView(withId(R.id.numberPicker1)).perform(clickBottomCentre) // 100min
        onView(withId(R.id.save_button)).perform(click())
        onView(withId(R.id.back_btn)).perform(click())

        // TimerListFragment PresetTimerが2つ以上存在する時はDetailLayout
        onView(withId(R.id.detail_timer_name)).check(matches(isDisplayed()))
        onView(withId(R.id.detail_timer_name)).check(matches(withText("newTimer")))
        onView(withId(R.id.detail_total_time)).check(matches(isDisplayed()))
        onView(withId(R.id.detail_total_time)).check(matches(withText("計 220分")))
        onView(withId(R.id.detail_notification_type_tv)).check(matches(isDisplayed()))
        onView(withId(R.id.detail_notification_type_tv)).check(matches(withText("バイブレーション")))
        onView(withId(R.id.detail_title)).perform(click())
        onView(withId(R.id.detail_detail)).check(matches(isDisplayed()))
        onView(withId(R.id.detail_detail)).check(matches(withText("newPreset1\t120分\t通知: 5分前\n" +
                "newPreset2\t100分\t通知: なし\n")))

        onView(withText("newTimer")).perform(click())
        onView(withId(R.id.preset_timer_list)).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                0, swipeRight()
            )
        )

        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(isDisplayed()))
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(withText("newPreset1を削除しました。")))
        onView(withText("newPreset1")).check(doesNotExist())
        onView(withId(R.id.back_btn)).perform(click())

        // presetTimerが残り1つなのでSimpleLayout
        onView(withId(R.id.simple_timer_name)).check(matches(isDisplayed()))
        onView(withId(R.id.simple_timer_name)).check(matches(withText("newTimer")))
        onView(withId(R.id.simple_total_time)).check(matches(isDisplayed()))
        onView(withId(R.id.simple_total_time)).check(matches(withText("計 100分")))
        onView(withId(R.id.simple_detail)).check(matches(isDisplayed()))
        onView(withId(R.id.simple_detail)).check(matches(withText("通知: なし")))

        onView(withText("newTimer")).perform(click())
        onView(withId(R.id.preset_timer_list)).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                0, swipeRight()
            )
        )

        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(isDisplayed()))
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(withText("newPreset2を削除しました。")))
        onView(withText("newPreset2")).check(doesNotExist())
        onView(withId(R.id.back_btn)).perform(click())

        // PresetTimerが存在しないのでTimer名のみが表示される
        onView(withId(R.id.simple_timer_name)).check(matches(isDisplayed()))
        onView(withId(R.id.simple_timer_name)).check(matches(withText("newTimer")))
        onView(withId(R.id.simple_total_time)).check(matches(isDisplayed()))
        onView(withId(R.id.simple_total_time)).check(matches(withText("")))
        onView(withId(R.id.simple_detail)).check(matches(isDisplayed()))
        onView(withId(R.id.simple_detail)).check(matches(withText("")))

        activityScenario.close()
    }

    @Test
    fun swipeAndDeletePresetTimer_RecoveryPresetTimer_updateTimer() = runBlocking {
        val timer = Timer("timer", 15000000, ListType.DETAIL_LAYOUT, NotificationType.VIBRATION, false,
            "preset1\t100分\t通知: 1分前\npreset2\t150分\t通知: なし\n")
        val preset1 = PresetTimer("timer", "preset1", 1, 6000000, 60000)
        val preset2 = PresetTimer("timer", "preset2", 2, 9000000)
        repository.insertTimerAndPresetTimers(timer, listOf(preset1,preset2))

        val activityScenario = ActivityScenario.launch(MainActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // TimerListFragmentで登録されているタイマーの確認
        onView(withId(R.id.detail_timer_name)).check(matches(isDisplayed()))
        onView(withId(R.id.detail_timer_name)).check(matches(withText("timer")))
        onView(withId(R.id.detail_total_time)).check(matches(isDisplayed()))
        onView(withId(R.id.detail_total_time)).check(matches(withText("計 250分")))
        onView(withId(R.id.detail_notification_type_tv)).check(matches(isDisplayed()))
        onView(withId(R.id.detail_notification_type_tv)).check(matches(withText("バイブレーション")))
        onView(withId(R.id.detail_title)).perform(click())
        onView(withId(R.id.detail_detail)).check(matches(isDisplayed()))
        onView(withId(R.id.detail_detail)).check(matches(withText("preset1\t100分\t通知: 1分前\n" +
                "preset2\t150分\t通知: なし\n")))

        onView(withText("timer")).perform(click())
        onView(withId(R.id.preset_timer_list)).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                0, swipeRight()
            )
        )

        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(isDisplayed()))
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(withText("preset1を削除しました。")))
        onView(withText("preset1")).check(doesNotExist())
        onView(withText("取り消し")).perform(click()) // recover
        onView(withText("preset1")).check(matches(isDisplayed()))
        onView(withId(R.id.back_btn)).perform(click())

        // TimerListFragmentで削除したタイマーが復活していることを確認
        onView(withId(R.id.detail_timer_name)).check(matches(isDisplayed()))
        onView(withId(R.id.detail_timer_name)).check(matches(withText("timer")))
        onView(withId(R.id.detail_total_time)).check(matches(isDisplayed()))
        onView(withId(R.id.detail_total_time)).check(matches(withText("計 250分")))
        onView(withId(R.id.detail_notification_type_tv)).check(matches(isDisplayed()))
        onView(withId(R.id.detail_notification_type_tv)).check(matches(withText("バイブレーション")))
        onView(withId(R.id.detail_title)).perform(click())
        onView(withId(R.id.detail_detail)).check(matches(isDisplayed()))
        onView(withId(R.id.detail_detail)).check(matches(withText("preset1\t100分\t通知: 1分前\n" +
                "preset2\t150分\t通知: なし\n")))

        activityScenario.close()
    }

    @Test
    fun insertMultiplePresetTimers_deletePresetTimersInDeletePresetTimerFragment_UpdateTimer() = runBlocking {
        val activityScenario = ActivityScenario.launch(MainActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // insert timer
        onView(withId(R.id.add_list)).perform(click())
        onView(withId(R.id.et_timer_name)).perform(click()).perform(replaceText("newTimer"), closeSoftKeyboard())
        onView(withText("保存")).perform(click())
        // PresetTimerListFragment
        onView(withId(R.id.et_timer_name)).check(matches(isDisplayed()))
        onView(withId(R.id.et_timer_name)).check(matches(withText("newTimer")))
        onView(withId(R.id.initial_message)).check(matches(isDisplayed()))
        onView(withId(R.id.add_list)).perform(click())
        // SetTimerFragment
        onView(withId(R.id.et_presetName)).check(matches(withText("presetTimer1"))) //デフォルトでpresetTimer1が指定されるか確認
        onView(withId(R.id.numberPicker1)).perform(clickBottomCentre)
        onView(withId(R.id.numberPicker2)).perform(clickBottomCentre)
        onView(withId(R.id.numberPicker2)).perform(clickBottomCentre) // 120min
        onView(withId(R.id.save_button)).perform(click())
        onView(withId(R.id.add_list)).perform(click())

        onView(withId(R.id.et_presetName)).check(matches(withText("presetTimer2"))) //デフォルトでpresetTimer2が指定されるか確認
        onView(withId(R.id.numberPicker1)).perform(clickBottomCentre) // 100min
        onView(withId(R.id.save_button)).perform(click())
        onView(withId(R.id.add_list)).perform(click())

        onView(withId(R.id.et_presetName)).check(matches(withText("presetTimer3"))) //デフォルトでpresetTimer3が指定されるか確認
        onView(withId(R.id.numberPicker1)).perform(clickBottomCentre)
        onView(withId(R.id.numberPicker2)).perform(clickBottomCentre)
        onView(withId(R.id.numberPicker2)).perform(clickBottomCentre)
        onView(withId(R.id.numberPicker2)).perform(clickBottomCentre)
        onView(withId(R.id.numberPicker2)).perform(clickBottomCentre)
        onView(withId(R.id.numberPicker2)).perform(clickBottomCentre) // 150min
        onView(withId(R.id.save_button)).perform(click())
        onView(withId(R.id.add_list)).perform(click())

        onView(withId(R.id.et_presetName)).check(matches(withText("presetTimer4"))) //デフォルトでpresetTimer4が指定されるか確認
        onView(withId(R.id.numberPicker1)).perform(clickBottomCentre) // 100min
        onView(withId(R.id.save_button)).perform(click())
        // TimerListFragmentで登録内容の確認
        onView(withId(R.id.back_btn)).perform(click())
        onView(withId(R.id.detail_timer_name)).check(matches(isDisplayed()))
        onView(withId(R.id.detail_timer_name)).check(matches(withText("newTimer")))
        onView(withId(R.id.detail_total_time)).check(matches(isDisplayed()))
        onView(withId(R.id.detail_total_time)).check(matches(withText("計 470分")))
        onView(withId(R.id.detail_notification_type_tv)).check(matches(isDisplayed()))
        onView(withId(R.id.detail_notification_type_tv)).check(matches(withText("バイブレーション")))
        onView(withId(R.id.detail_title)).perform(click())
        onView(withId(R.id.detail_detail)).check(matches(isDisplayed()))
        onView(withId(R.id.detail_detail)).check(matches(withText("presetTimer1\t120分\t通知: なし\n" +
                "presetTimer2\t100分\t通知: なし\npresetTimer3\t150分\t通知: なし\npresetTimer4\t100分\t通知: なし\n")))

        onView(withText("newTimer")).perform(click())
        // PresetTimerListFragmentで登録したタイマーが表示されるか確認
        onView(withText("presetTimer1")).check(matches(isDisplayed()))
        onView(withText("presetTimer2")).check(matches(isDisplayed()))
        onView(withText("presetTimer3")).check(matches(isDisplayed()))
        onView(withText("presetTimer4")).check(matches(isDisplayed()))
        onView(withId(R.id.delete_list)).perform(click())

        // タップをくり返してタイマーが正確に選択されるか確認　presetTimer2,3を削除
        onView(withId(R.id.delete_preset_timer_list)).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                0, click()
            )
        )

        onView(withId(R.id.delete_preset_timer_list)).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                1, click()
            )
        )

        onView(withId(R.id.delete_preset_timer_list)).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                0, click()
            )
        )

        onView(withId(R.id.delete_preset_timer_list)).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                2, click()
            )
        )

        onView(withId(R.id.delete_preset_timer_list)).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                1, click()
            )
        )

        onView(withId(R.id.delete_preset_timer_list)).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                1, click()
            )
        )
        onView(withId(R.id.preset_delete_btn)).perform(click())
        // PresetTimerListFragmentで削除したタイマーが消えているか確認
        onView(withText("presetTimer1")).check(matches(isDisplayed()))
        onView(withText("presetTimer2")).check(doesNotExist())
        onView(withText("presetTimer3")).check(doesNotExist())
        onView(withText("presetTimer4")).check(matches(isDisplayed()))

        onView(withId(R.id.back_btn)).perform(click())
        // TimerListFragmentで変更の確認
        onView(withId(R.id.detail_timer_name)).check(matches(isDisplayed()))
        onView(withId(R.id.detail_timer_name)).check(matches(withText("newTimer")))
        onView(withId(R.id.detail_total_time)).check(matches(isDisplayed()))
        onView(withId(R.id.detail_total_time)).check(matches(withText("計 220分")))
        onView(withId(R.id.detail_notification_type_tv)).check(matches(isDisplayed()))
        onView(withId(R.id.detail_notification_type_tv)).check(matches(withText("バイブレーション")))
        onView(withId(R.id.detail_title)).perform(click())
        onView(withId(R.id.detail_detail)).check(matches(isDisplayed()))
        onView(withId(R.id.detail_detail)).check(matches(withText(
                "presetTimer1\t120分\t通知: なし\npresetTimer4\t100分\t通知: なし\n")))

        activityScenario.close()
    }

    @Test
    fun cancelDeletePresetTimers_noChangeTimer() = runBlocking {
        val timer = Timer("timer", 30000000, ListType.DETAIL_LAYOUT, NotificationType.VIBRATION, false,
            "preset1\t100分\t通知: なし\npreset2\t150分\t通知: なし\npreset3\t100分\t通知: なし\n" +
                    "preset4\t150分\t通知: なし\n")
        val preset1 = PresetTimer("timer", "preset1", 1, 6000000)
        val preset2 = PresetTimer("timer", "preset2", 2, 9000000)
        val preset3 = PresetTimer("timer", "preset3", 3, 6000000)
        val preset4 = PresetTimer("timer", "preset4", 4, 9000000)
        repository.insertTimerAndPresetTimers(timer, listOf(preset1,preset2, preset3, preset4))

        val activityScenario = ActivityScenario.launch(MainActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // TimerListFragmentで登録内容の確認
        onView(withId(R.id.detail_timer_name)).check(matches(isDisplayed()))
        onView(withId(R.id.detail_timer_name)).check(matches(withText("timer")))
        onView(withId(R.id.detail_total_time)).check(matches(isDisplayed()))
        onView(withId(R.id.detail_total_time)).check(matches(withText("計 500分")))
        onView(withId(R.id.detail_notification_type_tv)).check(matches(isDisplayed()))
        onView(withId(R.id.detail_notification_type_tv)).check(matches(withText("バイブレーション")))
        onView(withId(R.id.detail_title)).perform(click())
        onView(withId(R.id.detail_detail)).check(matches(isDisplayed()))
        onView(withId(R.id.detail_detail)).check(matches(withText(
            "preset1\t100分\t通知: なし\npreset2\t150分\t通知: なし\n" +
                    "preset3\t100分\t通知: なし\npreset4\t150分\t通知: なし\n")))

        onView(withText("timer")).perform(click())
        // PresetTimerListFragment
        onView(withText("preset1")).check(matches(isDisplayed()))
        onView(withText("preset2")).check(matches(isDisplayed()))
        onView(withText("preset3")).check(matches(isDisplayed()))
        onView(withText("preset4")).check(matches(isDisplayed()))
        onView(withId(R.id.delete_list)).perform(click())
        // タップを繰り返しても正確に選択できるか確認 preset2,3を選択
        onView(withId(R.id.delete_preset_timer_list)).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                0, click()
            )
        )

        onView(withId(R.id.delete_preset_timer_list)).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                1, click()
            )
        )

        onView(withId(R.id.delete_preset_timer_list)).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                0, click()
            )
        )

        onView(withId(R.id.delete_preset_timer_list)).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                2, click()
            )
        )

        onView(withId(R.id.delete_preset_timer_list)).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                1, click()
            )
        )

        onView(withId(R.id.delete_preset_timer_list)).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                1, click()
            )
        )
        onView(withId(R.id.preset_cancel_btn)).perform(click())
        // PresetTimerListFragmentでタイマーが削除されていない事を確認
        onView(withText("preset1")).check(matches(isDisplayed()))
        onView(withText("preset2")).check(matches(isDisplayed()))
        onView(withText("preset3")).check(matches(isDisplayed()))
        onView(withText("preset4")).check(matches(isDisplayed()))
        onView(withId(R.id.back_btn)).perform(click())

        // TimerListFragmentで登録内容の確認
        onView(withId(R.id.detail_timer_name)).check(matches(isDisplayed()))
        onView(withId(R.id.detail_timer_name)).check(matches(withText("timer")))
        onView(withId(R.id.detail_total_time)).check(matches(isDisplayed()))
        onView(withId(R.id.detail_total_time)).check(matches(withText("計 500分")))
        onView(withId(R.id.detail_notification_type_tv)).check(matches(isDisplayed()))
        onView(withId(R.id.detail_notification_type_tv)).check(matches(withText("バイブレーション")))
        onView(withId(R.id.detail_title)).perform(click())
        onView(withId(R.id.detail_detail)).check(matches(isDisplayed()))
        onView(withId(R.id.detail_detail)).check(matches(withText(
            "preset1\t100分\t通知: なし\npreset2\t150分\t通知: なし\n" +
                    "preset3\t100分\t通知: なし\npreset4\t150分\t通知: なし\n")))

        activityScenario.close()
    }

    @Test
    fun insertTimerAndPresetTimers_startTimerFromTimerListFragment_cancelTimer() = runBlocking{
        val activityScenario = ActivityScenario.launch(MainActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // insert timer
        onView(withId(R.id.add_list)).perform(click())
        onView(withId(R.id.et_timer_name)).perform(click()).perform(replaceText("newTimer"), closeSoftKeyboard())
        onView(withText("保存")).perform(click())
        // PresetTimerListFragment
        onView(withId(R.id.et_timer_name)).check(matches(isDisplayed()))
        onView(withId(R.id.et_timer_name)).check(matches(withText("newTimer")))
        onView(withId(R.id.initial_message)).check(matches(isDisplayed()))
        onView(withId(R.id.add_list)).perform(click())
        onView(withId(R.id.et_presetName)).check(matches(withText("presetTimer1"))) //デフォルトでpresetTimer2が指定されるか確認
        onView(withId(R.id.numberPicker4)).perform(clickBottomCentre) // 10sec
        onView(withId(R.id.save_button)).perform(click())

        // PresetTimerListFragmentで登録の確認
        onView(withText("presetTimer1")).check(matches(isDisplayed()))
        onView(withText("10秒")).check(matches(isDisplayed()))
        onView(withId(R.id.back_btn)).perform(click())

        // TimerListFragmentで登録内容の確認
        onView(withId(R.id.simple_timer_name)).check(matches(isDisplayed()))
        onView(withId(R.id.simple_timer_name)).check(matches(withText("newTimer")))
        onView(withId(R.id.simple_total_time)).check(matches(isDisplayed()))
        onView(withId(R.id.simple_total_time)).check(matches(withText("計 10秒")))
        onView(withId(R.id.simple_detail)).check(matches(isDisplayed()))
        onView(withId(R.id.simple_detail)).check(matches(withText("通知: なし")))

        onView(withId(R.id.start_btn)).perform(click())
        onView(withId(R.id.cancel_btn)).perform(click())
        onView(withText("タイマーを終了しますか。")).check(matches(isDisplayed()))
        onView(withText("OK")).perform(click())

        // TimerListFragmentに戻る
        onView(withId(R.id.simple_timer_name)).check(matches(isDisplayed()))
        onView(withId(R.id.simple_timer_name)).check(matches(withText("newTimer")))
        onView(withId(R.id.simple_total_time)).check(matches(isDisplayed()))
        onView(withId(R.id.simple_total_time)).check(matches(withText("計 10秒")))
        onView(withId(R.id.simple_detail)).check(matches(isDisplayed()))
        onView(withId(R.id.simple_detail)).check(matches(withText("通知: なし")))

        activityScenario.close()
    }

    @Test
    fun startTimerFromTimerListFragment_FinishTimer() = runBlocking{
        val activityScenario = ActivityScenario.launch(MainActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // insert timer
        onView(withId(R.id.add_list)).perform(click())
        onView(withId(R.id.et_timer_name)).perform(click()).perform(replaceText("newTimer"), closeSoftKeyboard())
        onView(withText("保存")).perform(click())
        // PresetTimerListFragment
        onView(withId(R.id.et_timer_name)).check(matches(isDisplayed()))
        onView(withId(R.id.et_timer_name)).check(matches(withText("newTimer")))
        onView(withId(R.id.initial_message)).check(matches(isDisplayed()))
        onView(withId(R.id.add_list)).perform(click())
        // SetTimerFragment
        onView(withId(R.id.et_presetName)).check(matches(withText("presetTimer1"))) //デフォルトでpresetTimer1が指定されるか確認
        onView(withId(R.id.numberPicker4)).perform(clickBottomCentre)
        onView(withId(R.id.numberPicker4)).perform(clickBottomCentre) // 20sec
        onView(withId(R.id.pre_notification)).perform(click())
        onView(withId(R.id.numberPickerSec)).perform(clickBottomCentre)
        onView(withId(R.id.numberPickerSec)).perform(clickBottomCentre)
        onView(withId(R.id.numberPickerSec)).perform(clickBottomCentre)
        onView(withId(R.id.numberPickerSec)).perform(clickBottomCentre)
        onView(withId(R.id.numberPickerSec)).perform(clickBottomCentre) // 5min
        onView(withText("保存")).perform(click())
        onView(withId(R.id.setting_preset)).check(matches(withText("通知: 5秒 前")))
        onView(withId(R.id.save_button)).perform(click())
        onView(withId(R.id.add_list)).perform(click())

        onView(withId(R.id.et_presetName)).check(matches(withText("presetTimer2"))) //デフォルトでpresetTimer2が指定されるか確認
        onView(withId(R.id.numberPicker4)).perform(clickBottomCentre) // 10sec
        onView(withId(R.id.save_button)).perform(click())

        // PresetTimerListFragmentで登録の確認
        onView(withText("presetTimer1")).check(matches(isDisplayed()))
        onView(withText("presetTimer2")).check(matches(isDisplayed()))
        onView(withText("20秒")).check(matches(isDisplayed()))
        onView(withText("10秒")).check(matches(isDisplayed()))
        onView(withText("通知: 5秒 前")).check(matches(isDisplayed()))
        onView(withId(R.id.back_btn)).perform(click())

        // TimerListFragmentで登録内容の確認
        onView(withId(R.id.detail_timer_name)).check(matches(isDisplayed()))
        onView(withId(R.id.detail_timer_name)).check(matches(withText("newTimer")))
        onView(withId(R.id.detail_total_time)).check(matches(isDisplayed()))
        onView(withId(R.id.detail_total_time)).check(matches(withText("計 30秒")))
        onView(withId(R.id.detail_notification_type_tv)).check(matches(isDisplayed()))
        onView(withId(R.id.detail_notification_type_tv)).check(matches(withText("バイブレーション")))
        onView(withId(R.id.detail_title)).perform(click())
        onView(withId(R.id.detail_detail)).check(matches(isDisplayed()))
        onView(withId(R.id.detail_detail)).check(matches(withText(
            "presetTimer1\t20秒\t通知: 5秒前\npresetTimer2\t10秒\t通知: なし\n")))

        // TimerStart
        onView(withId(R.id.start_btn)).perform(click())
        onView(withText("presetTimer1")).check(matches(isDisplayed()))
        Thread.sleep(5000)
        // pause and restart
        onView(withId(R.id.stop_or_start_btn)).perform(click())
        Thread.sleep(2000)
        onView(withId(R.id.stop_or_start_btn)).perform(click())
        // cancel and restart
        onView(withId(R.id.cancel_btn)).perform(click())
        onView(withText("タイマーを終了しますか。")).check(matches(isDisplayed()))
        onView(withText("キャンセル")).perform(click())
        onView(withId(R.id.stop_or_start_btn)).perform(click())
        Thread.sleep(15000)
        onView(withText("presetTimer2")).check(matches(isDisplayed()))
        Thread.sleep(10000)
        onView(withText("タイマーが終了しました。")).check(matches(isDisplayed()))
        onView(withText("OK")).perform(click())

        // TimerListFragmentで登録内容の確認
        onView(withId(R.id.detail_timer_name)).check(matches(isDisplayed()))
        onView(withId(R.id.detail_timer_name)).check(matches(withText("newTimer")))
        onView(withId(R.id.detail_total_time)).check(matches(isDisplayed()))
        onView(withId(R.id.detail_total_time)).check(matches(withText("計 30秒")))
        onView(withId(R.id.detail_notification_type_tv)).check(matches(isDisplayed()))
        onView(withId(R.id.detail_notification_type_tv)).check(matches(withText("バイブレーション")))
        onView(withId(R.id.detail_title)).perform(click())
        onView(withId(R.id.detail_detail)).check(matches(isDisplayed()))
        onView(withId(R.id.detail_detail)).check(matches(withText(
            "presetTimer1\t20秒\t通知: 5秒前\npresetTimer2\t10秒\t通知: なし\n")))

        activityScenario.close()
    }

    @Test
    fun startTimerFromPresetTimerListFragment_cancelTimer() = runBlocking{
        val timer = Timer("timer", 15000, ListType.DETAIL_LAYOUT, NotificationType.VIBRATION, false,
            "preset1\t10秒\t通知: なし\npreset2\t5秒\t通知: なし\n")
        val preset1 = PresetTimer("timer", "preset1", 1, 10000)
        val preset2 = PresetTimer("timer", "preset2", 2, 5000)
        repository.insertTimerAndPresetTimers(timer, listOf(preset1,preset2))

        val activityScenario = ActivityScenario.launch(MainActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // PresetTimerListで登録されているタイマーを確認
        onView(withText("timer")).perform(click())
        onView(withText("preset1")).check(matches(isDisplayed()))
        onView(withText("preset2")).check(matches(isDisplayed()))
        onView(withText("10秒")).check(matches(isDisplayed()))
        onView(withText("5秒")).check(matches(isDisplayed()))

        onView(withId(R.id.start_btn)).perform(click())
        onView(withId(R.id.cancel_btn)).perform(click())
        onView(withText("タイマーを終了しますか。")).check(matches(isDisplayed()))
        onView(withText("OK")).perform(click())

        // PresetTimerListFragmentに戻る
        onView(withText("timer")).perform(click())
        onView(withText("preset1")).check(matches(isDisplayed()))
        onView(withText("preset2")).check(matches(isDisplayed()))
        onView(withText("10秒")).check(matches(isDisplayed()))
        onView(withText("5秒")).check(matches(isDisplayed()))

        activityScenario.close()
    }

    @Test
    fun startTimerFromPresetTimerListFragment_finishTimer() = runBlocking{
        val timer = Timer("timer", 15000, ListType.DETAIL_LAYOUT, NotificationType.VIBRATION, false,
            "preset1\t10秒\t通知: なし\npreset2\t5秒\t通知: なし\n")
        val preset1 = PresetTimer("timer", "preset1", 1, 10000)
        val preset2 = PresetTimer("timer", "preset2", 2, 5000)
        repository.insertTimerAndPresetTimers(timer, listOf(preset1,preset2))

        val activityScenario = ActivityScenario.launch(MainActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // PresetTimerListで登録されているタイマーを確認
        onView(withText("timer")).perform(click())
        onView(withText("preset1")).check(matches(isDisplayed()))
        onView(withText("preset2")).check(matches(isDisplayed()))
        onView(withText("10秒")).check(matches(isDisplayed()))
        onView(withText("5秒")).check(matches(isDisplayed()))
        // startTimer
        onView(withId(R.id.start_btn)).perform(click())
        onView(withText("preset1")).check(matches(isDisplayed()))
        Thread.sleep(10000)
        onView(withText("preset2")).check(matches(isDisplayed()))
        Thread.sleep(5000)
        onView(withText("タイマーが終了しました。")).check(matches(isDisplayed()))
        onView(withText("OK")).perform(click())
        // PresetTimerListFragmentに戻る
        onView(withText("preset1")).check(matches(isDisplayed()))
        onView(withText("preset2")).check(matches(isDisplayed()))
        onView(withText("10秒")).check(matches(isDisplayed()))
        onView(withText("5秒")).check(matches(isDisplayed()))

        activityScenario.close()
    }

    private val clickTopCentre =
        actionWithAssertions(
            GeneralClickAction(
                Tap.SINGLE,
                GeneralLocation.TOP_CENTER,
                Press.FINGER,
                InputDevice.SOURCE_UNKNOWN,
                MotionEvent.BUTTON_PRIMARY
            )
        )

    private val clickBottomCentre =
        actionWithAssertions(
            GeneralClickAction(
                Tap.SINGLE,
                GeneralLocation.BOTTOM_CENTER,
                Press.FINGER,
                InputDevice.SOURCE_UNKNOWN,
                MotionEvent.BUTTON_PRIMARY
            )
        )
}