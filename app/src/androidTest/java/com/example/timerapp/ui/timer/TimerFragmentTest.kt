package com.example.timerapp.ui.timer

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
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
class TimerFragmentTest {

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
    fun clickCancelButton_showCancelDialogAndPopBackStack() = runBlockingTest {
        val timer = Timer("timer", 16200000, ListType.DETAIL_LAYOUT, NotificationType.VIBRATION,
            false, "preset1\n150分\n通知: 10分前\tpreset2\n120分\n通知: なし\t")
        val preset1 = PresetTimer("timer", "preset1", 1, 9000000, 600000)
        val preset2 = PresetTimer("timer", "preset2", 2, 7200000)
        timerRepository.insertTimerAndPresetTimers(timer, listOf(preset1, preset2))
        val bundle = TimerFragmentArgs("timer").toBundle()
        val navController = Mockito.mock(NavController::class.java)
        val scenario = launchFragmentInContainer<TimerFragment>(bundle, R.style.Theme_TimerApp)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        onView(withId(R.id.cancel_btn)).perform(click())
        onView(withText("タイマーを終了しますか。")).check(matches(isDisplayed()))

        onView(withText("OK")).perform(click())

        verify(navController).popBackStack()
    }

    @Test
    fun clickCancelButton_showCancelDialogAndReStart() = runBlockingTest {
        val timer = Timer("timer", 10000, ListType.DETAIL_LAYOUT, NotificationType.VIBRATION,
            false, "通知: なし\t")
        val preset1 = PresetTimer("timer", "preset1", 1, 10000)
        timerRepository.insertTimerAndPresetTimers(timer, listOf(preset1))
        val bundle = TimerFragmentArgs("timer").toBundle()
        val navController = Mockito.mock(NavController::class.java)
        val scenario = launchFragmentInContainer<TimerFragment>(bundle, R.style.Theme_TimerApp)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        onView(withId(R.id.cancel_btn)).perform(click())
        onView(withText("タイマーを終了しますか。")).check(matches(isDisplayed()))

        onView(withText("キャンセル")).perform(click())
        onView(withId(R.id.stop_or_start_btn)).perform(click())

        Thread.sleep(10000)
        onView(withText("タイマーが終了しました。")).check(matches(isDisplayed()))
        onView(withText("OK")).perform(click())

        verify(navController).popBackStack()
    }

    @Test
    fun clickStopButton_AndReStart() = runBlockingTest {
        val timer = Timer("timer", 10000, ListType.DETAIL_LAYOUT, NotificationType.VIBRATION,
            false, "通知: なし\t")
        val preset1 = PresetTimer("timer", "preset1", 1, 10000)
        timerRepository.insertTimerAndPresetTimers(timer, listOf(preset1))
        val bundle = TimerFragmentArgs("timer").toBundle()
        val navController = Mockito.mock(NavController::class.java)
        val scenario = launchFragmentInContainer<TimerFragment>(bundle, R.style.Theme_TimerApp)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        onView(withId(R.id.stop_or_start_btn)).perform(click())
        Thread.sleep(3000)
        onView(withId(R.id.stop_or_start_btn)).perform(click())

        Thread.sleep(10000)
        onView(withText("タイマーが終了しました。")).check(matches(isDisplayed()))
        onView(withText("OK")).perform(click())

        verify(navController).popBackStack()
    }

    @Test
    fun insertMultiplePresetTimer_confirmCorrectChangePresetTimer() = runBlockingTest {
        val timer = Timer("timer", 12000, ListType.DETAIL_LAYOUT, NotificationType.VIBRATION,
            false, "preset1\n5秒\n通知: なし\tpreset2\n7秒\n通知: なし\t")
        val preset1 = PresetTimer("timer", "preset1", 1, 5000)
        val preset2 = PresetTimer("timer", "preset2", 2, 7000)
        timerRepository.insertTimerAndPresetTimers(timer, listOf(preset1, preset2))
        val bundle = TimerFragmentArgs("timer").toBundle()
        val navController = Mockito.mock(NavController::class.java)
        val scenario = launchFragmentInContainer<TimerFragment>(bundle, R.style.Theme_TimerApp)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        onView(withId(R.id.preset_timer_title)).check(matches(withText("preset1")))
        Thread.sleep(5000)
        onView(withId(R.id.preset_timer_title)).check(matches(withText("preset2")))
        Thread.sleep(7000)
        onView(withText("タイマーが終了しました。")).check(matches(isDisplayed()))
        onView(withText("OK")).perform(click())

        verify(navController).popBackStack()
    }
}