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
        // ??????????????????????????????
        onView(withId(R.id.initial_timer_message)).check(matches(isDisplayed()))
        // dialog????????????????????????????????????
        // ???????????????????????????????????????????????????????????????????????????
        onView(withId(R.id.add_list)).perform(click())
        onView(withId(R.id.et_timer_name)).perform(click()).perform(replaceText("LongLongLongLongLongTimerName"), closeSoftKeyboard())
        onView(withText("??????")).perform(click())
        onView(withText("??????????????????${Constants.MAX_NAME_LENGTH}?????????????????????"))
        // ?????????????????????????????????
        onView(withId(R.id.et_timer_name)).perform(click()).perform(replaceText("newTimer"), closeSoftKeyboard())
        onView(withText("??????")).perform(click())
        // PresetTimerFragment
        // ?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        onView(withId(R.id.et_timer_name)).check(matches(isDisplayed()))
        onView(withId(R.id.et_timer_name)).check(matches(withText("newTimer")))
        onView(withId(R.id.initial_message)).check(matches(isDisplayed()))
        // ????????????????????????????????????????????????(TimerListFragment)?????????????????????????????????
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
        onView(withText("??????")).perform(click())
        onView(withId(R.id.back_btn)).perform(click())
        // insertTimer - timer2 - preset1 -preset2
        onView(withId(R.id.add_list)).perform(click())
        onView(withId(R.id.et_timer_name)).perform(click()).perform(replaceText("timer2"), closeSoftKeyboard())
        onView(withText("??????")).perform(click())
        // presetTimer?????????
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

        // PresetTimerListFragment presetTimer??????????????????????????????
        onView(withText("preset1")).check(matches(isDisplayed()))
        onView(withText("preset2")).check(matches(isDisplayed()))
        onView(withText("100???")).check(matches(isDisplayed()))
        onView(withText("120???")).check(matches(isDisplayed()))
        onView(withId(R.id.back_btn)).perform(click()) // ???????????????

        // TimerListFragment
        onView(withText("timer1")).check(matches(isDisplayed()))
        onView(withText("timer2")).check(matches(isDisplayed()))
        onView(withText("??? 220???")).check(matches(isDisplayed()))
        onView(withId(R.id.detail_title)).perform(click())
        onView(withId(R.id.detail_detail)).check(matches(isDisplayed()))
        onView(withId(R.id.detail_detail)).check(matches(withText("preset1\t100???\t??????: ??????\n" +
                "preset2\t120???\t??????: ??????\n")))
        onView(withId(R.id.detail_title)).perform(click())

        // swipe and delete timer2
        onView(withId(R.id.timer_list)).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                1, swipeRight()
            )
        )
        // ???????????????????????????????????????
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(isDisplayed()))
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(withText("timer2????????????????????????")))
        onView(withText("timer2")).check(doesNotExist())
        onView(withText("????????????")).perform(click()) // recover
        Thread.sleep(3000)
        // ??????????????????????????????????????????????????????
        onView(withText("timer2")).check(matches(isDisplayed()))
        onView(withId(R.id.detail_title)).perform(click())
        onView(withId(R.id.detail_detail)).check(matches(isDisplayed()))
        onView(withId(R.id.detail_detail)).check(matches(withText("preset1\t100???\t??????: ??????\npreset2\t120???\t??????: ??????\n")))

        activityScenario.close()
    }

    @Test
    fun insertTimer_deleteTimer_AccompaniedByDeletionPresetTimers() = runBlocking {
        val activityScenario = ActivityScenario.launch(MainActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // insertTimer - timer1 - preset1 - preset2
        onView(withId(R.id.add_list)).perform(click())
        onView(withId(R.id.et_timer_name)).perform(click()).perform(replaceText("timer1"), closeSoftKeyboard())
        onView(withText("??????")).perform(click())

        // presetTimer?????????
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

        // PresetTimerListFragment presetTimer??????????????????????????????
        onView(withText("preset1")).check(matches(isDisplayed()))
        onView(withText("preset2")).check(matches(isDisplayed()))
        onView(withText("100???")).check(matches(isDisplayed()))
        onView(withText("120???")).check(matches(isDisplayed()))
        onView(withId(R.id.back_btn)).perform(click()) // ???????????????

        // swipe and delete
        onView(withId(R.id.timer_list)).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                0, swipeRight()
            )
        )

        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(isDisplayed()))
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(withText("timer1????????????????????????")))
        onView(withText("timer1")).check(doesNotExist())

        // ??????timer1??????????????????preset1???preset2???????????????
        onView(withId(R.id.add_list)).perform(click())
        onView(withId(R.id.et_timer_name)).perform(click()).perform(replaceText("timer1"), closeSoftKeyboard())
        onView(withText("??????")).perform(click())

        onView(withId(R.id.et_timer_name)).check(matches(withText("timer1")))
        onView(withText("preset1")).check(doesNotExist())
        onView(withText("preset2")).check(doesNotExist())

        activityScenario.close()
    }

    @Test
    fun deleteTimersInDeleteTimerListFragment() = runBlocking {
        val timer1 = Timer("timer1", 900000, ListType.SIMPLE_LAYOUT, NotificationType.VIBRATION, false,
            "??????: ??????")
        val preset1 = PresetTimer("timer1", "preset", 1, 900000)
        val timer2 = Timer("timer2", 1500000, ListType.DETAIL_LAYOUT, NotificationType.VIBRATION, false,
            "preset1\t10???\t??????: ??????\npreset2\t15???\t??????: ??????\n")
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
        // DeleteTimerListFragment???
        onView(withId(R.id.delete_list)).perform(click())

        // ????????????timer??????????????????
        onView(withText("timer1")).check(matches(isDisplayed()))
        onView(withText("timer2")).check(matches(isDisplayed()))
        onView(withText("timer3")).check(matches(isDisplayed()))
        onView(withText("timer4")).check(matches(isDisplayed()))
        onView(withText("timer5")).check(matches(isDisplayed()))
        // tap????????????????????????????????????????????????timer1,3,5?????????
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
        // TimerListFragment???????????????item??????????????????
        onView(withText("timer1")).check(doesNotExist())
        onView(withText("timer2")).check(matches(isDisplayed()))
        onView(withText("timer3")).check(doesNotExist())
        onView(withText("timer4")).check(matches(isDisplayed()))
        onView(withText("timer5")).check(doesNotExist())

        // DeleteTimerListFragment??????????????????item??????????????????????????????
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
            "??????: ??????")
        val preset1 = PresetTimer("timer1", "preset", 1, 900000)
        val timer2 = Timer("timer2", 1500000, ListType.DETAIL_LAYOUT, NotificationType.VIBRATION, false,
            "preset1\t10???\t??????: ??????\npreset2\t15???\t??????: ??????\n")
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
        // DeleteTimerListFragment???
        onView(withId(R.id.delete_list)).perform(click())

        // ??????????????????????????????????????????????????????
        onView(withText("timer1")).check(matches(isDisplayed()))
        onView(withText("timer2")).check(matches(isDisplayed()))
        onView(withText("timer3")).check(matches(isDisplayed()))
        onView(withText("timer4")).check(matches(isDisplayed()))
        onView(withText("timer5")).check(matches(isDisplayed()))
        // tap????????????????????????????????????????????????timer1,2,3?????????
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
        // TimerListFragment??????timer???????????????????????????
        onView(withText("timer1")).check(matches(isDisplayed()))
        onView(withText("timer2")).check(matches(isDisplayed()))
        onView(withText("timer3")).check(matches(isDisplayed()))
        onView(withText("timer4")).check(matches(isDisplayed()))
        onView(withText("timer5")).check(matches(isDisplayed()))

        // DeleteTimerListFragment??????timer???????????????????????????
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
        onView(withText("??????")).perform(click())
        // presetTimer?????????
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

        // TimerListFragment??????Timer???????????????
        onView(withId(R.id.back_btn)).perform(click())
        onView(withText("timer1")).check(matches(isDisplayed()))
        onView(withText("????????????????????????")).check(matches(isDisplayed()))

        onView(withId(R.id.timer_list)).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                0, click()
            )
        )

        // PresetTimerListFragment??????Timer?????????????????????
        onView(withId(R.id.et_timer_name)).check(matches(isDisplayed()))
        onView(withId(R.id.et_timer_name)).check(matches(withText("timer1")))
        onView(withText("preset1")).check(matches(isDisplayed()))
        onView(withText("preset2")).check(matches(isDisplayed()))
        onView(withText("100???")).check(matches(isDisplayed()))
        onView(withText("120???")).check(matches(isDisplayed()))
        // ???????????? ????????????????????????????????????
        onView(withId(R.id.et_timer_name)).perform(click()).perform(replaceText(""), closeSoftKeyboard())
        onView(withId(R.id.sounds_spinner)).perform(click())
        onView(withText("????????????")).perform(click())
        onView(withId(R.id.save_update)).perform(click())
        onView(withText("????????????????????????????????????????????????")).check(matches(isDisplayed()))
        // ?????????????????????????????????????????????
        onView(withId(R.id.et_timer_name)).perform(click()).perform(replaceText("updateTimer"), closeSoftKeyboard())
        onView(withId(R.id.save_update)).perform(click())
        // ???????????????
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(isDisplayed()))
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(withText("?????????????????????????????????????????????")))

        // TimerListFragment???????????????????????????????????????
        onView(withId(R.id.back_btn)).perform(click())
        onView(withText("updateTimer")).check(matches(isDisplayed()))
        onView(withText("????????????")).check(matches(isDisplayed()))
        activityScenario.close()
    }

    @Test
    fun addPresetTimerAndUpdateTimer() = runBlocking {
        val activityScenario = ActivityScenario.launch(MainActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // insert timer
        onView(withId(R.id.add_list)).perform(click())
        onView(withId(R.id.et_timer_name)).perform(click()).perform(replaceText("newTimer"), closeSoftKeyboard())
        onView(withText("??????")).perform(click())
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
        onView(withText("??????")).perform(click())
        onView(withId(R.id.setting_preset)).check(matches(withText("??????: 5??? ???")))
        onView(withId(R.id.save_button)).perform(click())

        // PresetTimerListFragment
        onView(withId(R.id.preset_timer_name)).check(matches(isDisplayed()))
        onView(withText("newPreset1")).check(matches(isDisplayed()))
        onView(withId(R.id.preset_timer_time)).check(matches(isDisplayed()))
        onView(withText("120???")).check(matches(isDisplayed()))
        onView(withId(R.id.preset_timer_prenotification_time)).check(matches(isDisplayed()))
        onView(withText("??????: 5??? ???")).check(matches(isDisplayed()))
        // TimerListFragment ???????????????????????????????????????????????????
        onView(withId(R.id.back_btn)).perform(click())
        onView(withText("??? 120???")).check(matches(isDisplayed()))
        onView(withText("??????: 5??????")).check(matches(isDisplayed()))

        // ?????????newTimer??????????????????????????????
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
        onView(withText("100???")).check(matches(isDisplayed()))
        // TimerListFragment DetailLayout?????????????????????????????????
        onView(withId(R.id.back_btn)).perform(click())
        onView(withId(R.id.detail_timer_name)).check(matches(isDisplayed()))
        onView(withId(R.id.detail_timer_name)).check(matches(withText("newTimer")))
        onView(withId(R.id.detail_total_time)).check(matches(isDisplayed()))
        onView(withId(R.id.detail_total_time)).check(matches(withText("??? 220???")))
        onView(withId(R.id.detail_title)).perform(click())
        onView(withId(R.id.detail_detail)).check(matches(isDisplayed()))
        onView(withId(R.id.detail_detail)).check(matches(withText("newPreset1\t120???\t??????: 5??????\n" +
                "newPreset2\t100???\t??????: ??????\n")))

        activityScenario.close()
    }

    @Test
    fun insertTimerAndPresetTimer_UpdatePresetTimerAndTimer() = runBlocking {
        val activityScenario = ActivityScenario.launch(MainActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // insert timer
        onView(withId(R.id.add_list)).perform(click())
        onView(withId(R.id.et_timer_name)).perform(click()).perform(replaceText("newTimer"), closeSoftKeyboard())
        onView(withText("??????")).perform(click())
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
        onView(withText("??????")).perform(click())
        onView(withId(R.id.setting_preset)).check(matches(withText("??????: 5??? ???")))
        onView(withId(R.id.save_button)).perform(click())
        onView(withId(R.id.add_list)).perform(click())

        onView(withId(R.id.et_presetName)).perform(click()).perform(replaceText("newPreset2"), closeSoftKeyboard())
        onView(withId(R.id.numberPicker1)).perform(clickBottomCentre)
        onView(withId(R.id.save_button)).perform(click())

        // PresetTimerListFragment
        onView(withText("newPreset1")).check(matches(isDisplayed()))
        onView(withText("newPreset2")).check(matches(isDisplayed()))
        onView(withText("120???")).check(matches(isDisplayed()))
        onView(withText("100???")).check(matches(isDisplayed()))
        onView(withId(R.id.back_btn)).perform(click())
        // TimerListFragment???timer??????????????????
        onView(withId(R.id.detail_timer_name)).check(matches(isDisplayed()))
        onView(withId(R.id.detail_timer_name)).check(matches(withText("newTimer")))
        onView(withId(R.id.detail_total_time)).check(matches(isDisplayed()))
        onView(withId(R.id.detail_total_time)).check(matches(withText("??? 220???")))
        onView(withId(R.id.detail_title)).perform(click())
        onView(withId(R.id.detail_detail)).check(matches(isDisplayed()))
        onView(withId(R.id.detail_detail)).check(matches(withText("newPreset1\t120???\t??????: 5??????\n" +
                "newPreset2\t100???\t??????: ??????\n")))

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
        onView(withId(R.id.setting_preset)).check(matches(withText("??????: 5??? ???")))
        // newPreset1?????????
        onView(withId(R.id.et_presetName)).perform(click()).perform(replaceText("updateTimer"))
        onView(withId(R.id.numberPicker2)).perform(clickBottomCentre)
        onView(withId(R.id.numberPicker2)).perform(clickBottomCentre)
        onView(withId(R.id.pre_notification)).perform(click())
        onView(withId(R.id.numberPickerMin)).perform(clickTopCentre)
        onView(withId(R.id.numberPickerMin)).perform(clickTopCentre)
        onView(withId(R.id.numberPickerMin)).perform(clickTopCentre)
        onView(withId(R.id.numberPickerMin)).perform(clickTopCentre)
        onView(withId(R.id.numberPickerMin)).perform(clickTopCentre)
        onView(withText("??????")).perform(click())
        onView(withId(R.id.setting_preset)).check(matches(withText("")))
        onView(withId(R.id.save_button)).perform(click())

        // PresetTimer
        onView(withText("updateTimer")).check(matches(isDisplayed()))
        onView(withText("140???")).check(matches(isDisplayed()))
        onView(withId(R.id.back_btn)).perform(click())

        // TimerListFragment
        onView(withText("newTimer")).check(matches(isDisplayed()))
        onView(withText("??? 240???")).check(matches(isDisplayed()))
        onView(withText("????????????????????????")).check(matches(isDisplayed()))
        onView(withId(R.id.detail_title)).perform(click())
        onView(withId(R.id.detail_detail)).check(matches(isDisplayed()))
        onView(withId(R.id.detail_detail)).check(matches(withText("updateTimer\t140???\t??????: ??????\n" +
                "newPreset2\t100???\t??????: ??????\n")))

        activityScenario.close()
    }

    @Test
    fun insertTimerAndPresetTimers_SwipeAndDeletePresetTimer_updateTimer() = runBlocking {
        val activityScenario = ActivityScenario.launch(MainActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // insert timer
        onView(withId(R.id.add_list)).perform(click())
        onView(withId(R.id.et_timer_name)).perform(click()).perform(replaceText("newTimer"), closeSoftKeyboard())
        onView(withText("??????")).perform(click())
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
        onView(withText("??????")).perform(click())
        onView(withId(R.id.setting_preset)).check(matches(withText("??????: 5??? ???")))
        onView(withId(R.id.save_button)).perform(click())
        onView(withId(R.id.add_list)).perform(click())

        onView(withId(R.id.et_presetName)).perform(click()).perform(replaceText("newPreset2"), closeSoftKeyboard())
        onView(withId(R.id.numberPicker1)).perform(clickBottomCentre) // 100min
        onView(withId(R.id.save_button)).perform(click())
        onView(withId(R.id.back_btn)).perform(click())

        // TimerListFragment PresetTimer???2???????????????????????????DetailLayout
        onView(withId(R.id.detail_timer_name)).check(matches(isDisplayed()))
        onView(withId(R.id.detail_timer_name)).check(matches(withText("newTimer")))
        onView(withId(R.id.detail_total_time)).check(matches(isDisplayed()))
        onView(withId(R.id.detail_total_time)).check(matches(withText("??? 220???")))
        onView(withId(R.id.detail_notification_type_tv)).check(matches(isDisplayed()))
        onView(withId(R.id.detail_notification_type_tv)).check(matches(withText("????????????????????????")))
        onView(withId(R.id.detail_title)).perform(click())
        onView(withId(R.id.detail_detail)).check(matches(isDisplayed()))
        onView(withId(R.id.detail_detail)).check(matches(withText("newPreset1\t120???\t??????: 5??????\n" +
                "newPreset2\t100???\t??????: ??????\n")))

        onView(withText("newTimer")).perform(click())
        onView(withId(R.id.preset_timer_list)).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                0, swipeRight()
            )
        )

        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(isDisplayed()))
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(withText("newPreset1????????????????????????")))
        onView(withText("newPreset1")).check(doesNotExist())
        onView(withId(R.id.back_btn)).perform(click())

        // presetTimer?????????1????????????SimpleLayout
        onView(withId(R.id.simple_timer_name)).check(matches(isDisplayed()))
        onView(withId(R.id.simple_timer_name)).check(matches(withText("newTimer")))
        onView(withId(R.id.simple_total_time)).check(matches(isDisplayed()))
        onView(withId(R.id.simple_total_time)).check(matches(withText("??? 100???")))
        onView(withId(R.id.simple_detail)).check(matches(isDisplayed()))
        onView(withId(R.id.simple_detail)).check(matches(withText("??????: ??????")))

        onView(withText("newTimer")).perform(click())
        onView(withId(R.id.preset_timer_list)).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                0, swipeRight()
            )
        )

        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(isDisplayed()))
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(withText("newPreset2????????????????????????")))
        onView(withText("newPreset2")).check(doesNotExist())
        onView(withId(R.id.back_btn)).perform(click())

        // PresetTimer????????????????????????Timer???????????????????????????
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
            "preset1\t100???\t??????: 1??????\npreset2\t150???\t??????: ??????\n")
        val preset1 = PresetTimer("timer", "preset1", 1, 6000000, 60000)
        val preset2 = PresetTimer("timer", "preset2", 2, 9000000)
        repository.insertTimerAndPresetTimers(timer, listOf(preset1,preset2))

        val activityScenario = ActivityScenario.launch(MainActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // TimerListFragment?????????????????????????????????????????????
        onView(withId(R.id.detail_timer_name)).check(matches(isDisplayed()))
        onView(withId(R.id.detail_timer_name)).check(matches(withText("timer")))
        onView(withId(R.id.detail_total_time)).check(matches(isDisplayed()))
        onView(withId(R.id.detail_total_time)).check(matches(withText("??? 250???")))
        onView(withId(R.id.detail_notification_type_tv)).check(matches(isDisplayed()))
        onView(withId(R.id.detail_notification_type_tv)).check(matches(withText("????????????????????????")))
        onView(withId(R.id.detail_title)).perform(click())
        onView(withId(R.id.detail_detail)).check(matches(isDisplayed()))
        onView(withId(R.id.detail_detail)).check(matches(withText("preset1\t100???\t??????: 1??????\n" +
                "preset2\t150???\t??????: ??????\n")))

        onView(withText("timer")).perform(click())
        onView(withId(R.id.preset_timer_list)).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                0, swipeRight()
            )
        )

        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(isDisplayed()))
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(withText("preset1????????????????????????")))
        onView(withText("preset1")).check(doesNotExist())
        onView(withText("????????????")).perform(click()) // recover
        onView(withText("preset1")).check(matches(isDisplayed()))
        onView(withId(R.id.back_btn)).perform(click())

        // TimerListFragment???????????????????????????????????????????????????????????????
        onView(withId(R.id.detail_timer_name)).check(matches(isDisplayed()))
        onView(withId(R.id.detail_timer_name)).check(matches(withText("timer")))
        onView(withId(R.id.detail_total_time)).check(matches(isDisplayed()))
        onView(withId(R.id.detail_total_time)).check(matches(withText("??? 250???")))
        onView(withId(R.id.detail_notification_type_tv)).check(matches(isDisplayed()))
        onView(withId(R.id.detail_notification_type_tv)).check(matches(withText("????????????????????????")))
        onView(withId(R.id.detail_title)).perform(click())
        onView(withId(R.id.detail_detail)).check(matches(isDisplayed()))
        onView(withId(R.id.detail_detail)).check(matches(withText("preset1\t100???\t??????: 1??????\n" +
                "preset2\t150???\t??????: ??????\n")))

        activityScenario.close()
    }

    @Test
    fun insertMultiplePresetTimers_deletePresetTimersInDeletePresetTimerFragment_UpdateTimer() = runBlocking {
        val activityScenario = ActivityScenario.launch(MainActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // insert timer
        onView(withId(R.id.add_list)).perform(click())
        onView(withId(R.id.et_timer_name)).perform(click()).perform(replaceText("newTimer"), closeSoftKeyboard())
        onView(withText("??????")).perform(click())
        // PresetTimerListFragment
        onView(withId(R.id.et_timer_name)).check(matches(isDisplayed()))
        onView(withId(R.id.et_timer_name)).check(matches(withText("newTimer")))
        onView(withId(R.id.initial_message)).check(matches(isDisplayed()))
        onView(withId(R.id.add_list)).perform(click())
        // SetTimerFragment
        onView(withId(R.id.et_presetName)).check(matches(withText("presetTimer1"))) //??????????????????presetTimer1???????????????????????????
        onView(withId(R.id.numberPicker1)).perform(clickBottomCentre)
        onView(withId(R.id.numberPicker2)).perform(clickBottomCentre)
        onView(withId(R.id.numberPicker2)).perform(clickBottomCentre) // 120min
        onView(withId(R.id.save_button)).perform(click())
        onView(withId(R.id.add_list)).perform(click())

        onView(withId(R.id.et_presetName)).check(matches(withText("presetTimer2"))) //??????????????????presetTimer2???????????????????????????
        onView(withId(R.id.numberPicker1)).perform(clickBottomCentre) // 100min
        onView(withId(R.id.save_button)).perform(click())
        onView(withId(R.id.add_list)).perform(click())

        onView(withId(R.id.et_presetName)).check(matches(withText("presetTimer3"))) //??????????????????presetTimer3???????????????????????????
        onView(withId(R.id.numberPicker1)).perform(clickBottomCentre)
        onView(withId(R.id.numberPicker2)).perform(clickBottomCentre)
        onView(withId(R.id.numberPicker2)).perform(clickBottomCentre)
        onView(withId(R.id.numberPicker2)).perform(clickBottomCentre)
        onView(withId(R.id.numberPicker2)).perform(clickBottomCentre)
        onView(withId(R.id.numberPicker2)).perform(clickBottomCentre) // 150min
        onView(withId(R.id.save_button)).perform(click())
        onView(withId(R.id.add_list)).perform(click())

        onView(withId(R.id.et_presetName)).check(matches(withText("presetTimer4"))) //??????????????????presetTimer4???????????????????????????
        onView(withId(R.id.numberPicker1)).perform(clickBottomCentre) // 100min
        onView(withId(R.id.save_button)).perform(click())
        // TimerListFragment????????????????????????
        onView(withId(R.id.back_btn)).perform(click())
        onView(withId(R.id.detail_timer_name)).check(matches(isDisplayed()))
        onView(withId(R.id.detail_timer_name)).check(matches(withText("newTimer")))
        onView(withId(R.id.detail_total_time)).check(matches(isDisplayed()))
        onView(withId(R.id.detail_total_time)).check(matches(withText("??? 470???")))
        onView(withId(R.id.detail_notification_type_tv)).check(matches(isDisplayed()))
        onView(withId(R.id.detail_notification_type_tv)).check(matches(withText("????????????????????????")))
        onView(withId(R.id.detail_title)).perform(click())
        onView(withId(R.id.detail_detail)).check(matches(isDisplayed()))
        onView(withId(R.id.detail_detail)).check(matches(withText("presetTimer1\t120???\t??????: ??????\n" +
                "presetTimer2\t100???\t??????: ??????\npresetTimer3\t150???\t??????: ??????\npresetTimer4\t100???\t??????: ??????\n")))

        onView(withText("newTimer")).perform(click())
        // PresetTimerListFragment??????????????????????????????????????????????????????
        onView(withText("presetTimer1")).check(matches(isDisplayed()))
        onView(withText("presetTimer2")).check(matches(isDisplayed()))
        onView(withText("presetTimer3")).check(matches(isDisplayed()))
        onView(withText("presetTimer4")).check(matches(isDisplayed()))
        onView(withId(R.id.delete_list)).perform(click())

        // ??????????????????????????????????????????????????????????????????????????????presetTimer2,3?????????
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
        // PresetTimerListFragment??????????????????????????????????????????????????????
        onView(withText("presetTimer1")).check(matches(isDisplayed()))
        onView(withText("presetTimer2")).check(doesNotExist())
        onView(withText("presetTimer3")).check(doesNotExist())
        onView(withText("presetTimer4")).check(matches(isDisplayed()))

        onView(withId(R.id.back_btn)).perform(click())
        // TimerListFragment??????????????????
        onView(withId(R.id.detail_timer_name)).check(matches(isDisplayed()))
        onView(withId(R.id.detail_timer_name)).check(matches(withText("newTimer")))
        onView(withId(R.id.detail_total_time)).check(matches(isDisplayed()))
        onView(withId(R.id.detail_total_time)).check(matches(withText("??? 220???")))
        onView(withId(R.id.detail_notification_type_tv)).check(matches(isDisplayed()))
        onView(withId(R.id.detail_notification_type_tv)).check(matches(withText("????????????????????????")))
        onView(withId(R.id.detail_title)).perform(click())
        onView(withId(R.id.detail_detail)).check(matches(isDisplayed()))
        onView(withId(R.id.detail_detail)).check(matches(withText(
                "presetTimer1\t120???\t??????: ??????\npresetTimer4\t100???\t??????: ??????\n")))

        activityScenario.close()
    }

    @Test
    fun cancelDeletePresetTimers_noChangeTimer() = runBlocking {
        val timer = Timer("timer", 30000000, ListType.DETAIL_LAYOUT, NotificationType.VIBRATION, false,
            "preset1\t100???\t??????: ??????\npreset2\t150???\t??????: ??????\npreset3\t100???\t??????: ??????\n" +
                    "preset4\t150???\t??????: ??????\n")
        val preset1 = PresetTimer("timer", "preset1", 1, 6000000)
        val preset2 = PresetTimer("timer", "preset2", 2, 9000000)
        val preset3 = PresetTimer("timer", "preset3", 3, 6000000)
        val preset4 = PresetTimer("timer", "preset4", 4, 9000000)
        repository.insertTimerAndPresetTimers(timer, listOf(preset1,preset2, preset3, preset4))

        val activityScenario = ActivityScenario.launch(MainActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // TimerListFragment????????????????????????
        onView(withId(R.id.detail_timer_name)).check(matches(isDisplayed()))
        onView(withId(R.id.detail_timer_name)).check(matches(withText("timer")))
        onView(withId(R.id.detail_total_time)).check(matches(isDisplayed()))
        onView(withId(R.id.detail_total_time)).check(matches(withText("??? 500???")))
        onView(withId(R.id.detail_notification_type_tv)).check(matches(isDisplayed()))
        onView(withId(R.id.detail_notification_type_tv)).check(matches(withText("????????????????????????")))
        onView(withId(R.id.detail_title)).perform(click())
        onView(withId(R.id.detail_detail)).check(matches(isDisplayed()))
        onView(withId(R.id.detail_detail)).check(matches(withText(
            "preset1\t100???\t??????: ??????\npreset2\t150???\t??????: ??????\n" +
                    "preset3\t100???\t??????: ??????\npreset4\t150???\t??????: ??????\n")))

        onView(withText("timer")).perform(click())
        // PresetTimerListFragment
        onView(withText("preset1")).check(matches(isDisplayed()))
        onView(withText("preset2")).check(matches(isDisplayed()))
        onView(withText("preset3")).check(matches(isDisplayed()))
        onView(withText("preset4")).check(matches(isDisplayed()))
        onView(withId(R.id.delete_list)).perform(click())
        // ??????????????????????????????????????????????????????????????? preset2,3?????????
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
        // PresetTimerListFragment??????????????????????????????????????????????????????
        onView(withText("preset1")).check(matches(isDisplayed()))
        onView(withText("preset2")).check(matches(isDisplayed()))
        onView(withText("preset3")).check(matches(isDisplayed()))
        onView(withText("preset4")).check(matches(isDisplayed()))
        onView(withId(R.id.back_btn)).perform(click())

        // TimerListFragment????????????????????????
        onView(withId(R.id.detail_timer_name)).check(matches(isDisplayed()))
        onView(withId(R.id.detail_timer_name)).check(matches(withText("timer")))
        onView(withId(R.id.detail_total_time)).check(matches(isDisplayed()))
        onView(withId(R.id.detail_total_time)).check(matches(withText("??? 500???")))
        onView(withId(R.id.detail_notification_type_tv)).check(matches(isDisplayed()))
        onView(withId(R.id.detail_notification_type_tv)).check(matches(withText("????????????????????????")))
        onView(withId(R.id.detail_title)).perform(click())
        onView(withId(R.id.detail_detail)).check(matches(isDisplayed()))
        onView(withId(R.id.detail_detail)).check(matches(withText(
            "preset1\t100???\t??????: ??????\npreset2\t150???\t??????: ??????\n" +
                    "preset3\t100???\t??????: ??????\npreset4\t150???\t??????: ??????\n")))

        activityScenario.close()
    }

    @Test
    fun insertTimerAndPresetTimers_startTimerFromTimerListFragment_cancelTimer() = runBlocking{
        val activityScenario = ActivityScenario.launch(MainActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // insert timer
        onView(withId(R.id.add_list)).perform(click())
        onView(withId(R.id.et_timer_name)).perform(click()).perform(replaceText("newTimer"), closeSoftKeyboard())
        onView(withText("??????")).perform(click())
        // PresetTimerListFragment
        onView(withId(R.id.et_timer_name)).check(matches(isDisplayed()))
        onView(withId(R.id.et_timer_name)).check(matches(withText("newTimer")))
        onView(withId(R.id.initial_message)).check(matches(isDisplayed()))
        onView(withId(R.id.add_list)).perform(click())
        onView(withId(R.id.et_presetName)).check(matches(withText("presetTimer1"))) //??????????????????presetTimer2???????????????????????????
        onView(withId(R.id.numberPicker4)).perform(clickBottomCentre) // 10sec
        onView(withId(R.id.save_button)).perform(click())

        // PresetTimerListFragment??????????????????
        onView(withText("presetTimer1")).check(matches(isDisplayed()))
        onView(withText("10???")).check(matches(isDisplayed()))
        onView(withId(R.id.back_btn)).perform(click())

        // TimerListFragment????????????????????????
        onView(withId(R.id.simple_timer_name)).check(matches(isDisplayed()))
        onView(withId(R.id.simple_timer_name)).check(matches(withText("newTimer")))
        onView(withId(R.id.simple_total_time)).check(matches(isDisplayed()))
        onView(withId(R.id.simple_total_time)).check(matches(withText("??? 10???")))
        onView(withId(R.id.simple_detail)).check(matches(isDisplayed()))
        onView(withId(R.id.simple_detail)).check(matches(withText("??????: ??????")))

        onView(withId(R.id.start_btn)).perform(click())
        onView(withId(R.id.cancel_btn)).perform(click())
        onView(withText("????????????????????????????????????")).check(matches(isDisplayed()))
        onView(withText("OK")).perform(click())

        // TimerListFragment?????????
        onView(withId(R.id.simple_timer_name)).check(matches(isDisplayed()))
        onView(withId(R.id.simple_timer_name)).check(matches(withText("newTimer")))
        onView(withId(R.id.simple_total_time)).check(matches(isDisplayed()))
        onView(withId(R.id.simple_total_time)).check(matches(withText("??? 10???")))
        onView(withId(R.id.simple_detail)).check(matches(isDisplayed()))
        onView(withId(R.id.simple_detail)).check(matches(withText("??????: ??????")))

        activityScenario.close()
    }

    @Test
    fun startTimerFromTimerListFragment_FinishTimer() = runBlocking{
        val activityScenario = ActivityScenario.launch(MainActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // insert timer
        onView(withId(R.id.add_list)).perform(click())
        onView(withId(R.id.et_timer_name)).perform(click()).perform(replaceText("newTimer"), closeSoftKeyboard())
        onView(withText("??????")).perform(click())
        // PresetTimerListFragment
        onView(withId(R.id.et_timer_name)).check(matches(isDisplayed()))
        onView(withId(R.id.et_timer_name)).check(matches(withText("newTimer")))
        onView(withId(R.id.initial_message)).check(matches(isDisplayed()))
        onView(withId(R.id.add_list)).perform(click())
        // SetTimerFragment
        onView(withId(R.id.et_presetName)).check(matches(withText("presetTimer1"))) //??????????????????presetTimer1???????????????????????????
        onView(withId(R.id.numberPicker4)).perform(clickBottomCentre)
        onView(withId(R.id.numberPicker4)).perform(clickBottomCentre) // 20sec
        onView(withId(R.id.pre_notification)).perform(click())
        onView(withId(R.id.numberPickerSec)).perform(clickBottomCentre)
        onView(withId(R.id.numberPickerSec)).perform(clickBottomCentre)
        onView(withId(R.id.numberPickerSec)).perform(clickBottomCentre)
        onView(withId(R.id.numberPickerSec)).perform(clickBottomCentre)
        onView(withId(R.id.numberPickerSec)).perform(clickBottomCentre) // 5min
        onView(withText("??????")).perform(click())
        onView(withId(R.id.setting_preset)).check(matches(withText("??????: 5??? ???")))
        onView(withId(R.id.save_button)).perform(click())
        onView(withId(R.id.add_list)).perform(click())

        onView(withId(R.id.et_presetName)).check(matches(withText("presetTimer2"))) //??????????????????presetTimer2???????????????????????????
        onView(withId(R.id.numberPicker4)).perform(clickBottomCentre) // 10sec
        onView(withId(R.id.save_button)).perform(click())

        // PresetTimerListFragment??????????????????
        onView(withText("presetTimer1")).check(matches(isDisplayed()))
        onView(withText("presetTimer2")).check(matches(isDisplayed()))
        onView(withText("20???")).check(matches(isDisplayed()))
        onView(withText("10???")).check(matches(isDisplayed()))
        onView(withText("??????: 5??? ???")).check(matches(isDisplayed()))
        onView(withId(R.id.back_btn)).perform(click())

        // TimerListFragment????????????????????????
        onView(withId(R.id.detail_timer_name)).check(matches(isDisplayed()))
        onView(withId(R.id.detail_timer_name)).check(matches(withText("newTimer")))
        onView(withId(R.id.detail_total_time)).check(matches(isDisplayed()))
        onView(withId(R.id.detail_total_time)).check(matches(withText("??? 30???")))
        onView(withId(R.id.detail_notification_type_tv)).check(matches(isDisplayed()))
        onView(withId(R.id.detail_notification_type_tv)).check(matches(withText("????????????????????????")))
        onView(withId(R.id.detail_title)).perform(click())
        onView(withId(R.id.detail_detail)).check(matches(isDisplayed()))
        onView(withId(R.id.detail_detail)).check(matches(withText(
            "presetTimer1\t20???\t??????: 5??????\npresetTimer2\t10???\t??????: ??????\n")))

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
        onView(withText("????????????????????????????????????")).check(matches(isDisplayed()))
        onView(withText("???????????????")).perform(click())
        onView(withId(R.id.stop_or_start_btn)).perform(click())
        Thread.sleep(15000)
        onView(withText("presetTimer2")).check(matches(isDisplayed()))
        Thread.sleep(10000)
        onView(withText("????????????????????????????????????")).check(matches(isDisplayed()))
        onView(withText("OK")).perform(click())

        // TimerListFragment????????????????????????
        onView(withId(R.id.detail_timer_name)).check(matches(isDisplayed()))
        onView(withId(R.id.detail_timer_name)).check(matches(withText("newTimer")))
        onView(withId(R.id.detail_total_time)).check(matches(isDisplayed()))
        onView(withId(R.id.detail_total_time)).check(matches(withText("??? 30???")))
        onView(withId(R.id.detail_notification_type_tv)).check(matches(isDisplayed()))
        onView(withId(R.id.detail_notification_type_tv)).check(matches(withText("????????????????????????")))
        onView(withId(R.id.detail_title)).perform(click())
        onView(withId(R.id.detail_detail)).check(matches(isDisplayed()))
        onView(withId(R.id.detail_detail)).check(matches(withText(
            "presetTimer1\t20???\t??????: 5??????\npresetTimer2\t10???\t??????: ??????\n")))

        activityScenario.close()
    }

    @Test
    fun startTimerFromPresetTimerListFragment_cancelTimer() = runBlocking{
        val timer = Timer("timer", 15000, ListType.DETAIL_LAYOUT, NotificationType.VIBRATION, false,
            "preset1\t10???\t??????: ??????\npreset2\t5???\t??????: ??????\n")
        val preset1 = PresetTimer("timer", "preset1", 1, 10000)
        val preset2 = PresetTimer("timer", "preset2", 2, 5000)
        repository.insertTimerAndPresetTimers(timer, listOf(preset1,preset2))

        val activityScenario = ActivityScenario.launch(MainActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // PresetTimerList?????????????????????????????????????????????
        onView(withText("timer")).perform(click())
        onView(withText("preset1")).check(matches(isDisplayed()))
        onView(withText("preset2")).check(matches(isDisplayed()))
        onView(withText("10???")).check(matches(isDisplayed()))
        onView(withText("5???")).check(matches(isDisplayed()))

        onView(withId(R.id.start_btn)).perform(click())
        onView(withId(R.id.cancel_btn)).perform(click())
        onView(withText("????????????????????????????????????")).check(matches(isDisplayed()))
        onView(withText("OK")).perform(click())

        // PresetTimerListFragment?????????
        onView(withText("timer")).perform(click())
        onView(withText("preset1")).check(matches(isDisplayed()))
        onView(withText("preset2")).check(matches(isDisplayed()))
        onView(withText("10???")).check(matches(isDisplayed()))
        onView(withText("5???")).check(matches(isDisplayed()))

        activityScenario.close()
    }

    @Test
    fun startTimerFromPresetTimerListFragment_finishTimer() = runBlocking{
        val timer = Timer("timer", 15000, ListType.DETAIL_LAYOUT, NotificationType.VIBRATION, false,
            "preset1\t10???\t??????: ??????\npreset2\t5???\t??????: ??????\n")
        val preset1 = PresetTimer("timer", "preset1", 1, 10000)
        val preset2 = PresetTimer("timer", "preset2", 2, 5000)
        repository.insertTimerAndPresetTimers(timer, listOf(preset1,preset2))

        val activityScenario = ActivityScenario.launch(MainActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // PresetTimerList?????????????????????????????????????????????
        onView(withText("timer")).perform(click())
        onView(withText("preset1")).check(matches(isDisplayed()))
        onView(withText("preset2")).check(matches(isDisplayed()))
        onView(withText("10???")).check(matches(isDisplayed()))
        onView(withText("5???")).check(matches(isDisplayed()))
        // startTimer
        onView(withId(R.id.start_btn)).perform(click())
        onView(withText("preset1")).check(matches(isDisplayed()))
        Thread.sleep(10000)
        onView(withText("preset2")).check(matches(isDisplayed()))
        Thread.sleep(5000)
        onView(withText("????????????????????????????????????")).check(matches(isDisplayed()))
        onView(withText("OK")).perform(click())
        // PresetTimerListFragment?????????
        onView(withText("preset1")).check(matches(isDisplayed()))
        onView(withText("preset2")).check(matches(isDisplayed()))
        onView(withText("10???")).check(matches(isDisplayed()))
        onView(withText("5???")).check(matches(isDisplayed()))

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