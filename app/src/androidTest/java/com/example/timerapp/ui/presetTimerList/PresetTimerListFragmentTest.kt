package com.example.timerapp.ui.presetTimerList

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.example.timerapp.R
import com.example.timerapp.ServiceLocator
import com.example.timerapp.database.ListType
import com.example.timerapp.database.NotificationType
import com.example.timerapp.database.PresetTimer
import com.example.timerapp.database.Timer
import com.example.timerapp.repository.FakeTimerAndroidTestRepository
import com.example.timerapp.repository.TimerRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.verify

@MediumTest
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class PresetTimerListFragmentTest {

    private lateinit var timerRepository: TimerRepository

    @Before
    fun createRepository() {
        timerRepository = FakeTimerAndroidTestRepository()
        ServiceLocator.timerRepository = timerRepository
    }

    @After
    fun cleanDb() = runBlockingTest {
        ServiceLocator.resetRepository()
    }

    @Test
    fun noPresetTimer_DisplayInitialMessageInUi() = runBlockingTest {
        val timer = Timer("timer")
        timerRepository.insertTimer(timer)
        val bundle = PresetTimerListFragmentArgs("timer").toBundle()
        launchFragmentInContainer<PresetTimerListFragment>(bundle, R.style.Theme_TimerApp)

        onView(withId(R.id.et_timer_name)).check(matches(isDisplayed()))
        onView(withId(R.id.et_timer_name)).check(matches(withText("timer")))
        onView(withId(R.id.initial_message)).check(matches(isDisplayed()))
    }

    @Test
    fun somePresetTimer_DisplayInUi() = runBlockingTest {
        val timer = Timer("timer", 16200000, ListType.DETAIL_LAYOUT, NotificationType.VIBRATION,
        false, "preset1\n150分\n通知: 10分前\tpreset2\n120分\n通知: なし\t")
        val preset1 = PresetTimer("timer", "preset1", 1, 9000000, 600000)
        val preset2 = PresetTimer("timer", "preset2", 2, 7200000)
        timerRepository.insertTimerAndPresetTimers(timer, listOf(preset1, preset2))
        val bundle = PresetTimerListFragmentArgs("timer").toBundle()
        launchFragmentInContainer<PresetTimerListFragment>(bundle, R.style.Theme_TimerApp)

        onView(withText("timer")).check(matches(isDisplayed()))
        onView(withText("preset1")).check(matches(isDisplayed()))
        onView(withText("preset2")).check(matches(isDisplayed()))
        onView(withText("150分")).check(matches(isDisplayed()))
        onView(withText("120分")).check(matches(isDisplayed()))
        onView(withText("通知: 10分 前")).check(matches(isDisplayed()))
    }

    @Test
    fun updateTimerName_invalidInput_showError() = runBlockingTest {
        val timer = Timer("timer")
        timerRepository.insertTimer(timer)
        val bundle = PresetTimerListFragmentArgs("timer").toBundle()
        launchFragmentInContainer<PresetTimerListFragment>(bundle, R.style.Theme_TimerApp)

        onView(withId(R.id.et_timer_name)).perform(click()).perform(replaceText(""), closeSoftKeyboard())
        onView(withId(R.id.save_update)).perform(click())

        onView(withText("タイマー名が入力されていません。")).check(matches(isDisplayed()))
    }

    @Test
    fun updateTimerName_validInput_changeTimerName() = runBlockingTest {
        val timer = Timer("timer")
        timerRepository.insertTimer(timer)
        val bundle = PresetTimerListFragmentArgs("timer").toBundle()
        launchFragmentInContainer<PresetTimerListFragment>(bundle, R.style.Theme_TimerApp)

        onView(withId(R.id.et_timer_name)).perform(click()).perform(replaceText("updateTimer"), closeSoftKeyboard())
        onView(withId(R.id.save_update)).perform(click())

        onView(withId(R.id.et_timer_name)).check(matches(withText("updateTimer")))
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(isDisplayed()))
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(withText("タイマーの設定を変更しました。")))
    }

    @Test
    fun updateTimerSound_changeSoundFromSpinnerMenu() = runBlockingTest {
        val timer = Timer("timer")
        timerRepository.insertTimer(timer)
        val bundle = PresetTimerListFragmentArgs("timer").toBundle()
        launchFragmentInContainer<PresetTimerListFragment>(bundle, R.style.Theme_TimerApp)

        onView(withId(R.id.sounds_spinner)).perform(click())
        onView(withText("アラーム")).perform(click())
        onView(withId(R.id.save_update)).perform(click())

        onView(withText("アラーム")).check(matches(isDisplayed()))
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(isDisplayed()))
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(withText("タイマーの設定を変更しました。")))
    }

    @Test
    fun swipePresetTimerItem_deletePresetTimer() = runBlockingTest {
        val timer = Timer("timer", 16200000, ListType.DETAIL_LAYOUT, NotificationType.VIBRATION,
            false, "preset1\n150分\n通知: 10分前\tpreset2\n120分\n通知: なし\t")
        val preset1 = PresetTimer("timer", "preset1", 1, 9000000, 600000)
        val preset2 = PresetTimer("timer", "preset2", 2, 7200000)
        timerRepository.insertTimerAndPresetTimers(timer, listOf(preset1, preset2))
        val bundle = PresetTimerListFragmentArgs("timer").toBundle()
        launchFragmentInContainer<PresetTimerListFragment>(bundle, R.style.Theme_TimerApp)

        onView(withText("preset1")).check(matches(isDisplayed()))
        onView(withText("preset2")).check(matches(isDisplayed()))

        onView(withId(R.id.preset_timer_list)).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, swipeRight())
        )

        Thread.sleep(1000)
        onView(withText("preset1")).check(doesNotExist())
        onView(withText("preset2")).check(matches(isDisplayed()))
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(isDisplayed()))
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(withText("preset1を削除しました。")))
    }

    @Test
    fun clickBackButton_navigateToTimerList() = runBlockingTest {
        val timer = Timer("timer")
        timerRepository.insertTimer(timer)
        val bundle = PresetTimerListFragmentArgs("timer").toBundle()
        val scenario = launchFragmentInContainer<PresetTimerListFragment>(bundle, R.style.Theme_TimerApp)
        val navController = Mockito.mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        onView(withId(R.id.back_btn)).perform(click())
        verify(navController).popBackStack()
    }

    @Test
    fun clickStartButton_noPresetTimerRegistration_showErrorOnSnackbar() = runBlockingTest {
        val timer = Timer("timer")
        timerRepository.insertTimer(timer)
        val bundle = PresetTimerListFragmentArgs("timer").toBundle()
        launchFragmentInContainer<PresetTimerListFragment>(bundle, R.style.Theme_TimerApp)

        onView(withId(R.id.start_btn)).perform(click())
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(isDisplayed()))
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(withText("タイマーが登録されていません。")))
    }

    @Test
    fun clickStartButton_somePresetTimerRegistration_navigateToTimerFragment() = runBlockingTest {
        val timer = Timer("timer", 16200000, ListType.DETAIL_LAYOUT, NotificationType.VIBRATION,
            false, "preset1\n150分\n通知: 10分前\tpreset2\n120分\n通知: なし\t")
        val preset1 = PresetTimer("timer", "preset1", 1, 9000000, 600000)
        val preset2 = PresetTimer("timer", "preset2", 2, 7200000)
        timerRepository.insertTimerAndPresetTimers(timer, listOf(preset1, preset2))
        val bundle = PresetTimerListFragmentArgs("timer").toBundle()
        val navController = Mockito.mock(NavController::class.java)
        val scenario = launchFragmentInContainer<PresetTimerListFragment>(bundle, R.style.Theme_TimerApp)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        onView(withId(R.id.start_btn)).perform(click())
        Mockito.verify(navController).navigate(
            PresetTimerListFragmentDirections.actionPresetTimerListFragmentToTimerFragment("timer")
        )
    }

    @Test
    fun clickDeleteButton_noPresetTimerRegistration_showErrorOnSnackbar() = runBlockingTest {
        val timer = Timer("timer")
        timerRepository.insertTimer(timer)
        val bundle = PresetTimerListFragmentArgs("timer").toBundle()
        launchFragmentInContainer<PresetTimerListFragment>(bundle, R.style.Theme_TimerApp)

        onView(withId(R.id.delete_list)).perform(click())
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(isDisplayed()))
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(withText("タイマーが登録されていません。")))
    }

    @Test
    fun clickDeleteButton_somePresetTimerRegistration_navigateToDeleteTimerFragment() = runBlockingTest {
        val timer = Timer("timer", 16200000, ListType.DETAIL_LAYOUT, NotificationType.VIBRATION,
            false, "preset1\n150分\n通知: 10分前\tpreset2\n120分\n通知: なし\t")
        val preset1 = PresetTimer("timer", "preset1", 1, 9000000, 600000)
        val preset2 = PresetTimer("timer", "preset2", 2, 7200000)
        timerRepository.insertTimerAndPresetTimers(timer, listOf(preset1, preset2))
        val bundle = PresetTimerListFragmentArgs("timer").toBundle()
        val navController = Mockito.mock(NavController::class.java)
        val scenario = launchFragmentInContainer<PresetTimerListFragment>(bundle, R.style.Theme_TimerApp)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        onView(withId(R.id.delete_list)).perform(click())
        verify(navController).navigate(
            PresetTimerListFragmentDirections.actionPresetTimerListFragmentToDeletePresetTimerListFragment("timer")
        )
    }

    @Test
    fun clickPresetTimerItem_inTheCaseOfUpdate_navigateToSetTimerFragment() = runBlockingTest {
        val timer = Timer("timer", 16200000, ListType.DETAIL_LAYOUT, NotificationType.VIBRATION,
            false, "preset1\n150分\n通知: 10分前\tpreset2\n120分\n通知: なし\t")
        val preset1 = PresetTimer("timer", "preset1", 1, 9000000, 600000)
        val preset2 = PresetTimer("timer", "preset2", 2, 7200000)
        timerRepository.insertTimerAndPresetTimers(timer, listOf(preset1, preset2))
        val bundle = PresetTimerListFragmentArgs("timer").toBundle()
        val navController = Mockito.mock(NavController::class.java)
        val scenario = launchFragmentInContainer<PresetTimerListFragment>(bundle, R.style.Theme_TimerApp)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        onView(withId(R.id.preset_timer_list)).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click())
        )

        verify(navController).navigate(
            PresetTimerListFragmentDirections.actionPresetTimerListFragmentToSetTimerFragment(preset1.presetTimerId, "timer")
        )
    }

    @Test
    fun navigateToAddButton_inTheCaseOfAdd_navigateToSetTimerFragment() = runBlockingTest {
        val timer = Timer("timer")
        timerRepository.insertTimer(timer)
        val bundle = PresetTimerListFragmentArgs("timer").toBundle()
        val navController = Mockito.mock(NavController::class.java)
        val scenario = launchFragmentInContainer<PresetTimerListFragment>(bundle, R.style.Theme_TimerApp)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        onView(withId(R.id.add_list)).perform(click())
        verify(navController).navigate(
            PresetTimerListFragmentDirections.actionPresetTimerListFragmentToSetTimerFragment(null, "timer")
        )
    }
}