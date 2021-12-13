package com.example.timerapp.ui.deleteTimerList

import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
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
class DeleteTimerListFragmentTest {
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
    fun onlyPlaceName_DisplayOnlyNameUi() = runBlockingTest {
        val timer = Timer("timer")
        timerRepository.insertTimer(timer)
        launchFragmentInContainer<DeleteTimerListFragment>(Bundle(), R.style.Theme_TimerApp)

        onView(withId(R.id.delete_simple_timer_name)).check(matches(isDisplayed()))
        onView(withId(R.id.delete_simple_timer_name)).check(matches(withText("timer")))
    }

    @Test
    fun timerWithOnePresetTimer_DisplayTimerAndPresetInfoInUi() = runBlockingTest {
        val timer = Timer("timer", 9000000, ListType.SIMPLE_LAYOUT, NotificationType.VIBRATION, false, "通知: 10分前")
        timerRepository.insertTimer(timer)
        launchFragmentInContainer<DeleteTimerListFragment>(Bundle(), R.style.Theme_TimerApp)

        onView(withId(R.id.delete_simple_timer_name)).check(matches(isDisplayed()))
        onView(withId(R.id.delete_simple_timer_name)).check(matches(withText("timer")))
        onView(withId(R.id.delete_simple_total_time)).check(matches(isDisplayed()))
        onView(withId(R.id.delete_simple_total_time)).check(matches(withText("計 150分")))
        onView(withId(R.id.delete_simple_detail)).check(matches(isDisplayed()))
        onView(withId(R.id.delete_simple_detail)).check(matches(withText("通知: 10分前")))
        onView(withId(R.id.delete_simple_notification_type)).check(matches(isDisplayed()))
    }

    @Test
    fun timerWithSomePresetTimer_DisplayDetailLayoutInUi() = runBlockingTest {
        val detail = "preset1\t150分\t通知: 10分前\npreset2\t150分\t通知: なし\npreset3\t150分\t通知: なし\n"
        val timer = Timer("timer", 27000000, ListType.DETAIL_LAYOUT, NotificationType.VIBRATION,false, detail)
        timerRepository.insertTimer(timer)
        launchFragmentInContainer<DeleteTimerListFragment>(Bundle(), R.style.Theme_TimerApp)

        onView(withId(R.id.delete_detail_timer_name)).check(matches(isDisplayed()))
        onView(withId(R.id.delete_detail_timer_name)).check(matches(withText("timer")))
        onView(withId(R.id.delete_detail_total_time)).check(matches(isDisplayed()))
        onView(withId(R.id.delete_detail_total_time)).check(matches(withText("計 450分")))
        onView(withId(R.id.delete_detail_notification_type)).check(matches(isDisplayed()))
        onView(withId(R.id.delete_detail_notification_type_tv)).check(matches(isDisplayed()))
        onView(withId(R.id.delete_detail_notification_type_tv)).check(matches(withText("バイブレーション")))

        onView(withId(R.id.detail_title)).perform(click())
        onView(withId(R.id.delete_detail_detail)).check(matches(isDisplayed()))
        onView(withId(R.id.delete_detail_detail)).check(matches(withText(detail)))
    }

    @Test
    fun clickTimerItemsAndClickDeleteButton_setNavigateToTimerListFragmentEvent() = runBlockingTest {
        val timer1 = Timer("timer1")
        val timer2 = Timer("timer2")
        val timer3 = Timer("timer3")
        timerRepository.insertTimer(timer1)
        timerRepository.insertTimer(timer2)
        timerRepository.insertTimer(timer3)
        val scenario = launchFragmentInContainer<DeleteTimerListFragment>(Bundle(), R.style.Theme_TimerApp)
        val navController = Mockito.mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        onView(withId(R.id.delete_timer_list)).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
            0, click()
        ))
        onView(withId(R.id.delete_timer_list)).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
            1, click()
        ))

        onView(withId(R.id.delete_btn)).perform(click())
        verify(navController).navigate(
            DeleteTimerListFragmentDirections.actionDeleteTimerListFragmentToTimerListFragment()
        )
    }

    @Test
    fun clickTimerItemsAndClickCancelButton_setNavigateToTimerListFragmentEvent() = runBlockingTest {
        val timer1 = Timer("timer1")
        val timer2 = Timer("timer2")
        val timer3 = Timer("timer3")
        timerRepository.insertTimer(timer1)
        timerRepository.insertTimer(timer2)
        timerRepository.insertTimer(timer3)
        val scenario = launchFragmentInContainer<DeleteTimerListFragment>(Bundle(), R.style.Theme_TimerApp)
        val navController = Mockito.mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        onView(withId(R.id.delete_timer_list)).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                0, click()
            ))
        onView(withId(R.id.delete_timer_list)).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                1, click()
            ))

        onView(withId(R.id.cancel_btn)).perform(click())
        verify(navController).navigate(
            DeleteTimerListFragmentDirections.actionDeleteTimerListFragmentToTimerListFragment()
        )
    }
}