package com.example.timerapp

import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.example.timerapp.database.ListType
import com.example.timerapp.database.NotificationType
import com.example.timerapp.database.PresetTimer
import com.example.timerapp.database.Timer
import com.example.timerapp.repository.TimerRepository
import com.example.timerapp.ui.MainActivity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class AppNavigationTest {
    private lateinit var timerRepository: TimerRepository
    private val dataBindingIdlingResource = DataBindingIdlingResource()

    @Before
    fun init() {
        timerRepository = ServiceLocator.provideTimerRepository(getApplicationContext())
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
    fun presetTimerListFragment_backButton_backToTimerListFragment() = runBlocking {
        val activityScenario = ActivityScenario.launch(MainActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)
        // TimerListFragment
        // ??????????????????????????????
        onView(withId(R.id.initial_timer_message)).check(matches(isDisplayed()))
        // dialog????????????????????????????????????
        // ???????????????????????????????????????????????????????????????????????????
        onView(withId(R.id.add_list)).perform(click())
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
    fun setTimerListFragment_backButton_PresetTimerList_backButtonOnUi_backToTimerListFragment() = runBlocking {
        val timer = Timer("timer", 15000000, ListType.DETAIL_LAYOUT, NotificationType.VIBRATION, false,
            "preset1\t100???\t??????: 1??????\npreset2\t150???\t??????: ??????\n")
        val preset1 = PresetTimer("timer", "preset1", 1, 6000000, 60000)
        val preset2 = PresetTimer("timer", "preset2", 2, 9000000)
        timerRepository.insertTimerAndPresetTimers(timer, listOf(preset1,preset2))
        val activityScenario = ActivityScenario.launch(MainActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // TimerListFragment
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

        // PresetTimerList
        onView(withText("preset1")).check(matches(isDisplayed()))
        onView(withText("preset2")).check(matches(isDisplayed()))
        onView(withText("100???")).check(matches(isDisplayed()))
        onView(withText("150???")).check(matches(isDisplayed()))
        onView(withText("??????: 1??? ???")).check(matches(isDisplayed()))
        onView(withId(R.id.preset_timer_list)).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                0, click()
            )
        )

        // setTimerFragment
        onView(withId(R.id.et_presetName)).check(matches(isDisplayed()))
        onView(withId(R.id.et_presetName)).check(matches(withText("preset1")))
        onView(withId(R.id.et_presetName)).perform(replaceText("updatePreset"))
        onView(withParent(withId(R.id.numberPicker1))).check(matches(withText("1")))
        onView(withParent(withId(R.id.numberPicker2))).check(matches(withText("0")))
        onView(withParent(withId(R.id.numberPicker3))).check(matches(withText("0")))
        onView(withParent(withId(R.id.numberPicker4))).check(matches(withText("0")))
        onView(withParent(withId(R.id.numberPicker5))).check(matches(withText("0")))
        onView(withId(R.id.setting_preset)).check(matches(isDisplayed()))
        onView(withId(R.id.setting_preset)).check(matches(withText("??????: 1??? ???")))
        onView(withId(R.id.back_btn)).perform(click())

        // PresetTimerList
        onView(withText("preset1")).check(matches(isDisplayed()))
        onView(withText("preset2")).check(matches(isDisplayed()))
        onView(withText("100???")).check(matches(isDisplayed()))
        onView(withText("150???")).check(matches(isDisplayed()))
        onView(withText("??????: 1??? ???")).check(matches(isDisplayed()))
        onView(withId(R.id.back_btn)).perform(click())

        // TimerListFragment
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
    fun presetTimerListFragment_backButtonOnDeviceFailed_backToTimerListFragmentUsingBackButtonOnUI() = runBlocking {
        val activityScenario = ActivityScenario.launch(MainActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)
        // TimerListFragment
        // ??????????????????????????????
        onView(withId(R.id.initial_timer_message)).check(matches(isDisplayed()))
        // dialog????????????????????????????????????
        // ???????????????????????????????????????????????????????????????????????????
        onView(withId(R.id.add_list)).perform(click())
        onView(withId(R.id.et_timer_name)).perform(click()).perform(replaceText("newTimer"), closeSoftKeyboard())
        onView(withText("??????")).perform(click())
        // PresetTimerFragment
        // ?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        onView(withId(R.id.et_timer_name)).check(matches(isDisplayed()))
        onView(withId(R.id.et_timer_name)).check(matches(withText("newTimer")))
        onView(withId(R.id.initial_message)).check(matches(isDisplayed()))
        // Timer update
        onView(withId(R.id.et_timer_name)).perform(click()).perform(replaceText("updateTimer"), closeSoftKeyboard())
        onView(withId(R.id.sounds_spinner)).perform(click())
        onView(withText("????????????")).perform(click())
        onView(withId(R.id.save_update)).perform(click())
        // ???????????????
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(isDisplayed()))
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(withText("?????????????????????????????????????????????")))
        // ?????????????????????TimerListFragment???????????????
        pressBack()
        onView(withId(R.id.back_btn)).perform(click()) // ????????????????????????
        onView(withId(R.id.simple_timer_name)).check(matches(isDisplayed()))
        onView(withId(R.id.simple_timer_name)).check(matches(withText("updateTimer")))

        activityScenario.close()
    }

    @Test
    fun setTimerFragment_backButtonOnDeviceFailed_backToPresetTimerListAndTimerListFragment() = runBlocking {
        val timer = Timer("timer", 15000000, ListType.DETAIL_LAYOUT, NotificationType.VIBRATION, false,
            "preset1\t100???\t??????: 1??????\npreset2\t150???\t??????: ??????\n")
        val preset1 = PresetTimer("timer", "preset1", 1, 6000000, 60000)
        val preset2 = PresetTimer("timer", "preset2", 2, 9000000)
        timerRepository.insertTimerAndPresetTimers(timer, listOf(preset1,preset2))
        val activityScenario = ActivityScenario.launch(MainActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // TimerListFragment
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

        // PresetTimerList
        onView(withText("preset1")).check(matches(isDisplayed()))
        onView(withText("preset2")).check(matches(isDisplayed()))
        onView(withText("100???")).check(matches(isDisplayed()))
        onView(withText("150???")).check(matches(isDisplayed()))
        onView(withText("??????: 1??? ???")).check(matches(isDisplayed()))
        onView(withId(R.id.preset_timer_list)).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                0, click()
            )
        )

        // setTimerFragment
        onView(withId(R.id.et_presetName)).check(matches(isDisplayed()))
        onView(withId(R.id.et_presetName)).check(matches(withText("preset1")))
        onView(withId(R.id.et_presetName)).perform(replaceText("updatePreset"))
        onView(withParent(withId(R.id.numberPicker1))).check(matches(withText("1")))
        onView(withParent(withId(R.id.numberPicker2))).check(matches(withText("0")))
        onView(withParent(withId(R.id.numberPicker3))).check(matches(withText("0")))
        onView(withParent(withId(R.id.numberPicker4))).check(matches(withText("0")))
        onView(withParent(withId(R.id.numberPicker5))).check(matches(withText("0")))
        onView(withId(R.id.setting_preset)).check(matches(withText("??????: 1??? ???")))

        // updateName
        onView(withId(R.id.et_presetName)).perform(replaceText("updatePreset"))

        pressBack()
        Thread.sleep(1000)
        pressBack()

        // TimerListFragment???????????????????????????
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

        activityScenario.close()
    }

    @Test
    fun timerFragmentFromTimerListFragment_backButtonOnDevice_backToTimerListFragment() = runBlocking {
        val timer = Timer("timer", 15000, ListType.DETAIL_LAYOUT, NotificationType.VIBRATION, false,
            "preset1\t10???\t??????: 3??????\npreset2\t5???\t??????: ??????\n")
        val preset1 = PresetTimer("timer", "preset1", 1, 10000, 3000)
        val preset2 = PresetTimer("timer", "preset2", 2, 5000)
        timerRepository.insertTimerAndPresetTimers(timer, listOf(preset1,preset2))
        val activityScenario = ActivityScenario.launch(MainActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // TimerListFragment
        onView(withText("timer")).check(matches(isDisplayed()))
        onView(withText("??? 15???")).check(matches(isDisplayed()))
        onView(withText("????????????????????????")).check(matches(isDisplayed()))
        onView(withId(R.id.detail_title)).perform(click())
        onView(withId(R.id.detail_detail)).check(matches(isDisplayed()))
        onView(withId(R.id.detail_detail)).check(matches(withText("preset1\t10???\t??????: 3??????\npreset2\t5???\t??????: ??????\n")))
        onView(withId(R.id.start_btn)).perform(click())

        // TimerFragment
        onView(withId(R.id.preset_timer_title)).check(matches(isDisplayed()))
        onView(withId(R.id.preset_timer_title)).check(matches(withText("preset1")))
        pressBack()

        // TimerListFragment
        onView(withText("timer")).check(matches(isDisplayed()))
        onView(withText("??? 15???")).check(matches(isDisplayed()))
        onView(withText("????????????????????????")).check(matches(isDisplayed()))
        onView(withId(R.id.detail_title)).perform(click())
        onView(withId(R.id.detail_detail)).check(matches(isDisplayed()))
        onView(withId(R.id.detail_detail)).check(matches(withText("preset1\t10???\t??????: 3??????\npreset2\t5???\t??????: ??????\n")))
        onView(withId(R.id.start_btn)).perform(click())

        activityScenario.close()
    }

    @Test
    fun deleteTimerListFragment_backButtonOnDevice_backToTimerListFragment() = runBlocking {
        val timer = Timer("timer", 15000000, ListType.DETAIL_LAYOUT, NotificationType.VIBRATION, false,
            "preset1\t100???\t??????: 1??????\npreset2\t150???\t??????: ??????\n")
        val preset1 = PresetTimer("timer", "preset1", 1, 6000000, 60000)
        val preset2 = PresetTimer("timer", "preset2", 2, 9000000)
        timerRepository.insertTimerAndPresetTimers(timer, listOf(preset1,preset2))
        val activityScenario = ActivityScenario.launch(MainActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // TimerListFragment
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
        onView(withId(R.id.delete_list)).perform(click())

        // DeleteTimerList
        onView(withId(R.id.delete_detail_timer_name)).check(matches(isDisplayed()))
        onView(withId(R.id.delete_detail_timer_name)).check(matches(withText("timer")))
        onView(withId(R.id.delete_detail_total_time)).check(matches(isDisplayed()))
        onView(withId(R.id.delete_detail_total_time)).check(matches(withText("??? 250???")))
        onView(withId(R.id.delete_detail_notification_type_tv)).check(matches(isDisplayed()))
        onView(withId(R.id.delete_detail_notification_type_tv)).check(matches(withText("????????????????????????")))
        onView(withId(R.id.detail_title)).perform(click())
        onView(withId(R.id.delete_detail_detail)).check(matches(isDisplayed()))
        onView(withId(R.id.delete_detail_detail)).check(matches(withText("preset1\t100???\t??????: 1??????\n" +
                "preset2\t150???\t??????: ??????\n")))

        pressBack()

        // TimerListFragment
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
        onView(withId(R.id.delete_list)).perform(click())

        activityScenario.close()
    }

    @Test
    fun deletePresetTimerList_pressBackButton_backToPresetTimerList_pressBackButton_backToTimerListFragment() = runBlocking {
        val timer = Timer("timer", 15000000, ListType.DETAIL_LAYOUT, NotificationType.VIBRATION, false,
            "preset1\t100???\t??????: 1??????\npreset2\t150???\t??????: ??????\n")
        val preset1 = PresetTimer("timer", "preset1", 1, 6000000, 60000)
        val preset2 = PresetTimer("timer", "preset2", 2, 9000000)
        timerRepository.insertTimerAndPresetTimers(timer, listOf(preset1,preset2))
        val activityScenario = ActivityScenario.launch(MainActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)
        // TimerListFragment
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
        // PresetTimerListFragment
        onView(withText("preset1")).check(matches(isDisplayed()))
        onView(withText("preset2")).check(matches(isDisplayed()))
        onView(withText("100???")).check(matches(isDisplayed()))
        onView(withText("150???")).check(matches(isDisplayed()))
        onView(withText("??????: 1??? ???")).check(matches(isDisplayed()))
        onView(withId(R.id.delete_list)).perform(click())

        // deletePresetTimerList
        onView(withText("preset1")).check(matches(isDisplayed()))
        onView(withText("preset2")).check(matches(isDisplayed()))
        onView(withText("100???")).check(matches(isDisplayed()))
        onView(withText("150???")).check(matches(isDisplayed()))
        onView(withText("??????: 1??? ???")).check(matches(isDisplayed()))
        pressBack()

        // PresetTimerListFragment
        onView(withText("preset1")).check(matches(isDisplayed()))
        onView(withText("preset2")).check(matches(isDisplayed()))
        onView(withText("100???")).check(matches(isDisplayed()))
        onView(withText("150???")).check(matches(isDisplayed()))
        onView(withText("??????: 1??? ???")).check(matches(isDisplayed()))
        pressBack()

        // TimerListFragment
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

}