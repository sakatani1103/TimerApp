package com.example.timerapp.ui.timerlist

import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.example.timerapp.R
import com.example.timerapp.ServiceLocator
import com.example.timerapp.adapter.TimerListAdapter
import com.example.timerapp.database.ListType
import com.example.timerapp.database.NotificationType
import com.example.timerapp.database.Timer
import com.example.timerapp.repository.FakeTimerAndroidTestRepository
import com.example.timerapp.repository.TimerRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.core.IsNot.not
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.verify

@MediumTest
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class TimerListFragmentTest {
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
    fun noTimer_DisplayInitialMessageInUi() = runBlockingTest {
        launchFragmentInContainer<TimerListFragment>(Bundle(), R.style.Theme_TimerApp)

        onView(withId(R.id.initial_message)).check(matches(isDisplayed()))
    }

    @Test
    fun insertOnlyPlaceName_DisplayOnlyNameUi() = runBlockingTest {
        val timer = Timer("timer")
        timerRepository.insertTimer(timer)
        launchFragmentInContainer<TimerListFragment>(Bundle(), R.style.Theme_TimerApp)

        onView(withId(R.id.simple_timer_name)).check(matches(isDisplayed()))
        onView(withId(R.id.simple_timer_name)).check(matches(withText("timer")))
    }

    @Test
    fun insertTimerWithOnePresetTimer_DisplayTimerAndPresetInfoInUi() = runBlockingTest {
        val timer = Timer("timer", 9000000, ListType.SIMPLE_LAYOUT, NotificationType.VIBRATION, false, "通知: 10分前")
        timerRepository.insertTimer(timer)
        launchFragmentInContainer<TimerListFragment>(Bundle(), R.style.Theme_TimerApp)

        onView(withId(R.id.simple_timer_name)).check(matches(isDisplayed()))
        onView(withId(R.id.simple_timer_name)).check(matches(withText("timer")))
        onView(withId(R.id.simple_total_time)).check(matches(isDisplayed()))
        onView(withId(R.id.simple_total_time)).check(matches(withText("計 150分")))
        onView(withId(R.id.simple_detail)).check(matches(isDisplayed()))
        onView(withId(R.id.simple_detail)).check(matches(withText("通知: 10分前")))
        onView(withId(R.id.simple_notification_type)).check(matches(isDisplayed()))
    }

    @Test
    fun insertTimerWithSomePresetTimer_DisplayDetailLayoutInUi() = runBlockingTest {
        val detail = "preset1\t150分\t通知: 10分前\npreset2\t150分\t通知: なし\npreset3\t150分\t通知: なし\n"
        val timer = Timer("timer", 27000000, ListType.DETAIL_LAYOUT, NotificationType.VIBRATION,false, detail)
        timerRepository.insertTimer(timer)
        launchFragmentInContainer<TimerListFragment>(Bundle(), R.style.Theme_TimerApp)

        onView(withId(R.id.detail_timer_name)).check(matches(isDisplayed()))
        onView(withId(R.id.detail_timer_name)).check(matches(withText("timer")))
        onView(withId(R.id.detail_total_time)).check(matches(isDisplayed()))
        onView(withId(R.id.detail_total_time)).check(matches(withText("計 450分")))
        onView(withId(R.id.detail_notification_type)).check(matches(isDisplayed()))
        onView(withId(R.id.detail_notification_type_tv)).check(matches(isDisplayed()))
        onView(withId(R.id.detail_notification_type_tv)).check(matches(withText("バイブレーション")))

        onView(withId(R.id.detail_title)).perform(click())
        onView(withId(R.id.detail_detail)).check(matches(isDisplayed()))
        onView(withId(R.id.detail_detail)).check(matches(withText(detail)))
    }

    @Test
    fun swipeTimerItem_deleteTimer() = runBlockingTest {
        val timer1 = Timer("timer1")
        val timer2 = Timer("timer2")
        timerRepository.insertTimer(timer1)
        timerRepository.insertTimer(timer2)
        launchFragmentInContainer<TimerListFragment>(Bundle(), R.style.Theme_TimerApp)

        onView(withText("timer1")).check(matches(isDisplayed()))
        onView(withText("timer2")).check(matches(isDisplayed()))

        onView(withId(R.id.timer_list)).perform(
            RecyclerViewActions.actionOnItemAtPosition<TimerListAdapter.TimerViewHolder>(0, swipeRight())
        )

        onView(withText("timer1")).check(matches(not(isDisplayed())))
        onView(withText("timer2")).check(matches(isDisplayed()))
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(isDisplayed()))
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(withText("timer1を削除しました。")))
    }

    @Test
    fun clickTimerItemWithSimpleLayout_navigateToPresetTimerFragment() = runBlockingTest {
        val timer1 = Timer("timer1")
        val timer2 = Timer("timer2")
        timerRepository.insertTimer(timer1)
        timerRepository.insertTimer(timer2)
        val scenario = launchFragmentInContainer<TimerListFragment>(Bundle(), R.style.Theme_TimerApp)
        val navController = Mockito.mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        onView(withId(R.id.timer_list)).perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
            0, click()
        ))
        verify(navController).navigate(
            TimerListFragmentDirections.actionTimerListFragmentToPresetTimerListFragment("timer1")
        )
    }

    @Test
    fun clickTimerItemWithDetailLayout_navigateToPresetTimerFragment() = runBlockingTest {
        val detail = "preset1\t150分\t通知: 10分前\npreset2\t150分\t通知: なし\npreset3\t150分\t通知: なし\n"
        val timer = Timer("timer", 27000000, ListType.DETAIL_LAYOUT, NotificationType.VIBRATION,false, detail)
        timerRepository.insertTimer(timer)
        val scenario = launchFragmentInContainer<TimerListFragment>(Bundle(), R.style.Theme_TimerApp)
        val navController = Mockito.mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        onView(withId(R.id.timer_list)).perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
            0, click()
        ))

        verify(navController).navigate(
            TimerListFragmentDirections.actionTimerListFragmentToPresetTimerListFragment("timer")
        )
    }

    @Test
    fun startButtonOnTimerItemWithSimpleLayout_navigateToTimer() = runBlockingTest {
        val timer1 = Timer("timer1", 9000000, ListType.SIMPLE_LAYOUT, NotificationType.VIBRATION,false, "通知： なし")
        timerRepository.insertTimer(timer1)
        val scenario = launchFragmentInContainer<TimerListFragment>(Bundle(), R.style.Theme_TimerApp)
        val navController = Mockito.mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        onView(withId(R.id.simple_start_btn)).perform(click())

        verify(navController).navigate(
            TimerListFragmentDirections.actionTimerListFragmentToTimerFragment("timer1")
        )
    }

    @Test
    fun startButtonOnTimerItemWithDetailLayout_navigateToTimer() = runBlockingTest {
        val detail = "preset1\t150分\t通知: 10分前\npreset2\t150分\t通知: なし\npreset3\t150分\t通知: なし\n"
        val timer = Timer("timer", 27000000, ListType.DETAIL_LAYOUT, NotificationType.VIBRATION,false, detail)
        timerRepository.insertTimer(timer)
        val scenario = launchFragmentInContainer<TimerListFragment>(Bundle(), R.style.Theme_TimerApp)
        val navController = Mockito.mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        onView(withId(R.id.detail_start_btn)).perform(click())

        verify(navController).navigate(
            TimerListFragmentDirections.actionTimerListFragmentToTimerFragment("timer")
        )
    }

    @Test
    fun startButtonOnTimerItemWithZeroPresetTimer_createErrorOnSnackbar() = runBlockingTest {
        val timer1 = Timer("timer1", 0, ListType.SIMPLE_LAYOUT, NotificationType.VIBRATION,false, "no presetTimer")
        timerRepository.insertTimer(timer1)
        launchFragmentInContainer<TimerListFragment>(Bundle(), R.style.Theme_TimerApp)

        onView(withId(R.id.simple_start_btn)).perform(click())

        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(isDisplayed()))
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(withText("タイマーが設定されていません。")))
    }

    @Test
    fun clickAddButtonWithManyTimer_createErrorOnSnackbar() = runBlockingTest {
        val timerList = listOf(Timer("timer1"), Timer("timer2"), Timer("timer3"),
        Timer("timer4") ,Timer("timer5"), Timer("timer6"), Timer("timer7"),
        Timer("timer8"), Timer("timer9"), Timer("timer10"), Timer("timer11"),
        Timer("timer12"), Timer("timer13"), Timer("timer14"), Timer("timer15"))
        timerList.forEach { timerRepository.insertTimer(it) }
        launchFragmentInContainer<TimerListFragment>(Bundle(), R.style.Theme_TimerApp)

        onView(withId(R.id.add_list)).perform(click())
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(isDisplayed()))
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(withText("登録できるタイマーは15までです。")))
    }

    @Test
    fun clickAddButton_showDialog_andInputInvalidTimerName_showErrorOnDialog() = runBlockingTest {
        launchFragmentInContainer<TimerListFragment>(Bundle(), R.style.Theme_TimerApp)

        onView(withId(R.id.add_list)).perform(click())
        onView(withId(R.id.et_timer_name)).perform(click()).perform(replaceText(""), closeSoftKeyboard())
        onView(withText("保存")).perform(click())

        onView(withText("タイマー名が入力されていません。")).check(matches(isDisplayed()))
    }

    @Test
    fun clickAddButton_showDialog_andSaveTimerName_navigateToPresetTimer() = runBlockingTest {
        val scenario = launchFragmentInContainer<TimerListFragment>(Bundle(), R.style.Theme_TimerApp)
        val navController = Mockito.mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        onView(withId(R.id.add_list)).perform(click())
        onView(withId(R.id.et_timer_name)).perform(click()).perform(replaceText("timer"), closeSoftKeyboard())
        onView(withText("保存")).perform(click())

        verify(navController).navigate(
            TimerListFragmentDirections.actionTimerListFragmentToPresetTimerListFragment("timer")
        )
    }

    fun clickDeleteButtonWithNoTimer_createErrorOnSnackbar() = runBlockingTest {
        val timer1 = Timer("timer1", 0, ListType.SIMPLE_LAYOUT, NotificationType.VIBRATION,false, "no presetTimer")
        timerRepository.insertTimer(timer1)
        launchFragmentInContainer<TimerListFragment>(Bundle(), R.style.Theme_TimerApp)

        onView(withId(R.id.delete_list)).perform(click())
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(isDisplayed()))
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(withText("タイマーが設定されていません。")))
    }

    fun clickDeleteButton_navigateToDeleteTimerListFragment() = runBlockingTest {
        val timer1 = Timer("timer1", 9000000, ListType.SIMPLE_LAYOUT, NotificationType.VIBRATION,false, "通知: なし")
        timerRepository.insertTimer(timer1)
        val scenario = launchFragmentInContainer<TimerListFragment>(Bundle(), R.style.Theme_TimerApp)
        val navController = Mockito.mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        onView(withId(R.id.delete_list)).perform(click())
        verify(navController).navigate(
            TimerListFragmentDirections.actionTimerListFragmentToDeleteTimerListFragment()
        )
    }

}