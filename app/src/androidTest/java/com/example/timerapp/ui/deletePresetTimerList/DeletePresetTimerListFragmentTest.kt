package com.example.timerapp.ui.deletePresetTimerList

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
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
class DeletePresetTimerListFragmentTest{

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
    fun somePresetTimer_DisplayInUi() = runBlockingTest {
        val timer = Timer("timer", 18000000, ListType.DETAIL_LAYOUT, NotificationType.VIBRATION,
            false, "preset1\n150分\n通知: 10分前\tpreset2\n150分\n通知: なし\t")
        val preset1 = PresetTimer("timer", "preset1", 1, 9000000, 600000)
        val preset2 = PresetTimer("timer", "preset2", 2, 7200000)
        timerRepository.insertTimerAndPresetTimers(timer, listOf(preset1, preset2))
        val bundle = DeletePresetTimerListFragmentArgs("timer").toBundle()
        launchFragmentInContainer<DeletePresetTimerListFragment>(bundle, R.style.Theme_TimerApp)

        onView(withText("preset1")).check(matches(isDisplayed()))
        onView(withText("preset2")).check(matches(isDisplayed()))
        onView(withText("150分")).check(matches(isDisplayed()))
        onView(withText("120分")).check(matches(isDisplayed()))
        onView(withText("通知: 10分 前")).check(matches(isDisplayed()))
    }

    @Test
    fun clickPresetTimerItemsAndClickAndDeleteButton_setNavigateToPresetTimerEvent() = runBlockingTest {
        val timer = Timer("timer", 18000000, ListType.DETAIL_LAYOUT, NotificationType.VIBRATION,
            false, "preset1\n150分\n通知: 10分前\tpreset2\n150分\n通知: なし\t")
        val preset1 = PresetTimer("timer", "preset1", 1, 9000000, 600000)
        val preset2 = PresetTimer("timer", "preset2", 2, 7200000)
        timerRepository.insertTimerAndPresetTimers(timer, listOf(preset1, preset2))
        val bundle = DeletePresetTimerListFragmentArgs("timer").toBundle()
        val scenario = launchFragmentInContainer<DeletePresetTimerListFragment>(bundle, R.style.Theme_TimerApp)
        val navController = Mockito.mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        onView(withId(R.id.delete_preset_timer_list)).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                0, click()
            ))
        onView(withId(R.id.delete_preset_timer_list)).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
            1, click()
        ))

        onView(withId(R.id.preset_delete_btn)).perform(click())
        verify(navController).navigate(
            DeletePresetTimerListFragmentDirections.actionDeletePresetTimerListFragmentToPresetTimerListFragment("timer")
        )
    }

    @Test
    fun clickPresetTimerItemsAndClickAndCancelButton_setNavigateToPresetTimerEvent() = runBlockingTest {
        val timer = Timer("timer", 18000000, ListType.DETAIL_LAYOUT, NotificationType.VIBRATION,
            false, "preset1\n150分\n通知: 10分前\tpreset2\n150分\n通知: なし\t")
        val preset1 = PresetTimer("timer", "preset1", 1, 9000000, 600000)
        val preset2 = PresetTimer("timer", "preset2", 2, 7200000)
        timerRepository.insertTimerAndPresetTimers(timer, listOf(preset1, preset2))
        val bundle = DeletePresetTimerListFragmentArgs("timer").toBundle()
        val scenario = launchFragmentInContainer<DeletePresetTimerListFragment>(bundle, R.style.Theme_TimerApp)
        val navController = Mockito.mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        onView(withId(R.id.delete_preset_timer_list)).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                0, click()
            ))
        onView(withId(R.id.delete_preset_timer_list)).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                1, click()
            ))

        onView(withId(R.id.preset_cancel_btn)).perform(ViewActions.click())
        verify(navController).navigate(
            DeletePresetTimerListFragmentDirections.actionDeletePresetTimerListFragmentToPresetTimerListFragment("timer")
        )
    }

}