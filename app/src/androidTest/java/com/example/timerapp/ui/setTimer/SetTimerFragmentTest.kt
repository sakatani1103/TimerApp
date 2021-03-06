package com.example.timerapp.ui.setTimer

import android.view.InputDevice
import android.view.MotionEvent
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.GeneralClickAction
import androidx.test.espresso.action.GeneralLocation
import androidx.test.espresso.action.Press
import androidx.test.espresso.action.Tap
import androidx.test.espresso.action.ViewActions.*
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
import com.example.timerapp.others.Constants
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
class SetTimerFragmentTest {

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
    fun inTheCaseOfAdd_noValueSetInUi() = runBlockingTest {
        val timer = Timer("timer", 16200000, ListType.DETAIL_LAYOUT, NotificationType.VIBRATION,
            false, "preset1\n150???\n??????: 10??????\tpreset2\n120???\n??????: ??????\t")
        val preset1 = PresetTimer("timer", "preset1", 1, 9000000, 600000)
        val preset2 = PresetTimer("timer", "preset2", 2, 7200000)
        timerRepository.insertTimerAndPresetTimers(timer, listOf(preset1, preset2))
        val bundle = SetTimerFragmentArgs(null, "timer").toBundle()
        launchFragmentInContainer<SetTimerFragment>(bundle, R.style.Theme_TimerApp)

        onView(withId(R.id.et_presetName)).check(matches(isDisplayed()))
        onView(withId(R.id.et_presetName)).check(matches(withText("presetTimer3")))
        onView(withParent(withId(R.id.numberPicker1))).check(matches(withText("0")))
        onView(withParent(withId(R.id.numberPicker2))).check(matches(withText("0")))
        onView(withParent(withId(R.id.numberPicker3))).check(matches(withText("0")))
        onView(withParent(withId(R.id.numberPicker4))).check(matches(withText("0")))
        onView(withParent(withId(R.id.numberPicker5))).check(matches(withText("0")))
    }

    @Test
    fun inTheCaseOfUpdate_registeredValueSettingInUi() = runBlockingTest {
        val timer = Timer("timer", 16200000, ListType.DETAIL_LAYOUT, NotificationType.VIBRATION,
            false, "preset1\n150???\n??????: 10??????\tpreset2\n120???\n??????: ??????\t")
        val preset1 = PresetTimer("timer", "preset1", 1, 9000000, 600000)
        val preset2 = PresetTimer("timer", "preset2", 2, 7200000)
        timerRepository.insertTimerAndPresetTimers(timer, listOf(preset1, preset2))
        val bundle = SetTimerFragmentArgs(preset1.presetTimerId, "timer").toBundle()
        launchFragmentInContainer<SetTimerFragment>(bundle, R.style.Theme_TimerApp)

        onView(withId(R.id.et_presetName)).check(matches(isDisplayed()))
        onView(withId(R.id.et_presetName)).check(matches(withText("preset1")))
        onView(withParent(withId(R.id.numberPicker1))).check(matches(withText("1")))
        onView(withParent(withId(R.id.numberPicker2))).check(matches(withText("5")))
        onView(withParent(withId(R.id.numberPicker3))).check(matches(withText("0")))
        onView(withParent(withId(R.id.numberPicker4))).check(matches(withText("0")))
        onView(withParent(withId(R.id.numberPicker5))).check(matches(withText("0")))
        onView(withId(R.id.setting_preset)).check(matches(isDisplayed()))
        onView(withId(R.id.setting_preset)).check(matches(withText("??????: 10??? ???")))
    }

    // NumberPicker????????????????????????????????????Error???????????????????????????1???????????????
    @Test
    fun inTheCaseOfAdd_inputInvalidName_showError() = runBlockingTest {
        val timer = Timer("timer", 16200000, ListType.DETAIL_LAYOUT, NotificationType.VIBRATION,
            false, "preset1\n150???\n??????: 10??????\tpreset2\n120???\n??????: ??????\t")
        val preset1 = PresetTimer("timer", "preset1", 1, 9000000, 600000)
        val preset2 = PresetTimer("timer", "preset2", 2, 7200000)
        timerRepository.insertTimerAndPresetTimers(timer, listOf(preset1, preset2))
        val bundle = SetTimerFragmentArgs(null, "timer").toBundle()
        launchFragmentInContainer<SetTimerFragment>(bundle, R.style.Theme_TimerApp)

        onView(withId(R.id.numberPicker1)).perform(clickBottomCentre)
        onView(withId(R.id.et_presetName)).perform(click()).perform(replaceText("longLongLongLongPresetTimerName"), closeSoftKeyboard())
        onView(withId(R.id.save_button)).perform(click())

        onView(withText("??????????????????${Constants.MAX_NAME_LENGTH}?????????????????????")).check(matches(isDisplayed()))
    }

    @Test
    fun inTheCaseOfAdd_zeroPresetTime_showErrorOnSnackbar() = runBlockingTest {
        val timer = Timer("timer", 16200000, ListType.DETAIL_LAYOUT, NotificationType.VIBRATION,
            false, "preset1\n150???\n??????: 10??????\tpreset2\n120???\n??????: ??????\t")
        val preset1 = PresetTimer("timer", "preset1", 1, 9000000, 600000)
        val preset2 = PresetTimer("timer", "preset2", 2, 7200000)
        timerRepository.insertTimerAndPresetTimers(timer, listOf(preset1, preset2))
        val bundle = SetTimerFragmentArgs(null, "timer").toBundle()
        launchFragmentInContainer<SetTimerFragment>(bundle, R.style.Theme_TimerApp)

        onView(withId(R.id.save_button)).perform(click())
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(isDisplayed()))
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(withText("??????????????????????????????????????????????????????")))
    }

    @Test
    fun inTheCaseOfAdd_validInput_setNavigateToPresetTimerListEvent() = runBlockingTest {
        val timer = Timer("timer", 16200000, ListType.DETAIL_LAYOUT, NotificationType.VIBRATION,
            false, "preset1\n150???\n??????: 10??????\tpreset2\n150???\n??????: ??????\t")
        val preset1 = PresetTimer("timer", "preset1", 1, 9000000, 600000)
        val preset2 = PresetTimer("timer", "preset2", 2, 7200000)
        timerRepository.insertTimerAndPresetTimers(timer, listOf(preset1, preset2))
        val bundle = SetTimerFragmentArgs(null, "timer").toBundle()
        val navController = Mockito.mock(NavController::class.java)
        val scenario = launchFragmentInContainer<SetTimerFragment>(bundle, R.style.Theme_TimerApp)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        onView(withId(R.id.numberPicker1)).perform(clickBottomCentre)
        onView(withId(R.id.save_button)).perform(click())

        verify(navController).popBackStack()
    }

    @Test
    fun inTheCaseOfUpdate_inputInvalidName_showError() = runBlockingTest {
        val timer = Timer("timer", 16200000, ListType.DETAIL_LAYOUT, NotificationType.VIBRATION,
            false, "preset1\n150???\n??????: 10??????\tpreset2\n120???\n??????: ??????\t")
        val preset1 = PresetTimer("timer", "preset1", 1, 9000000, 600000)
        val preset2 = PresetTimer("timer", "preset2", 2, 7200000)
        timerRepository.insertTimerAndPresetTimers(timer, listOf(preset1, preset2))
        val bundle = SetTimerFragmentArgs(preset1.presetTimerId, "timer").toBundle()
        launchFragmentInContainer<SetTimerFragment>(bundle, R.style.Theme_TimerApp)

        onView(withId(R.id.et_presetName)).perform(click()).perform(replaceText("longLongLongLongPresetTimerName"), closeSoftKeyboard())
        onView(withId(R.id.save_button)).perform(click())

        onView(withText("??????????????????${Constants.MAX_NAME_LENGTH}?????????????????????")).check(matches(isDisplayed()))
    }


    @Test
    fun inTheCaseOfUpdate_zeroPresetTime_showErrorOnSnackbar() = runBlockingTest {
        val timer = Timer("timer", 16200000, ListType.DETAIL_LAYOUT, NotificationType.VIBRATION,
            false, "preset1\n150???\n??????: 10??????\tpreset2\n120???\n??????: ??????\t")
        val preset1 = PresetTimer("timer", "preset1", 1, 9000000, 600000)
        val preset2 = PresetTimer("timer", "preset2", 2, 7200000)
        timerRepository.insertTimerAndPresetTimers(timer, listOf(preset1, preset2))
        val bundle = SetTimerFragmentArgs(preset1.presetTimerId, "timer").toBundle()
        launchFragmentInContainer<SetTimerFragment>(bundle, R.style.Theme_TimerApp)

        onView(withId(R.id.numberPicker1)).perform(clickTopCentre)
        onView(withId(R.id.numberPicker2)).perform(clickTopCentre)
        onView(withId(R.id.numberPicker2)).perform(clickTopCentre)
        onView(withId(R.id.numberPicker2)).perform(clickTopCentre)
        onView(withId(R.id.numberPicker2)).perform(clickTopCentre)
        onView(withId(R.id.numberPicker2)).perform(clickTopCentre)
        onView(withId(R.id.save_button)).perform(click())
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(isDisplayed()))
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(withText("??????????????????????????????????????????????????????")))
    }

    @Test
    fun inTheCaseOfUpdate_validInput_setNavigateToPresetTimerListEvent() = runBlockingTest {
        val timer = Timer("timer", 16200000, ListType.DETAIL_LAYOUT, NotificationType.VIBRATION,
            false, "preset1\n150???\n??????: 10??????\tpreset2\n120???\n??????: ??????\t")
        val preset1 = PresetTimer("timer", "preset1", 1, 9000000, 600000)
        val preset2 = PresetTimer("timer", "preset2", 2, 7200000)
        timerRepository.insertTimerAndPresetTimers(timer, listOf(preset1, preset2))
        val bundle = SetTimerFragmentArgs(preset1.presetTimerId, "timer").toBundle()
        val navController = Mockito.mock(NavController::class.java)
        val scenario = launchFragmentInContainer<SetTimerFragment>(bundle, R.style.Theme_TimerApp)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        onView(withId(R.id.numberPicker1)).perform(clickTopCentre)
        onView(withId(R.id.et_presetName)).perform(click()).perform(replaceText("updatePreset"))
        onView(withId(R.id.save_button)).perform(click())

        verify(navController).popBackStack()
    }

    @Test
    fun setPreNotificationTime5Min1sec_displayPreNotificationTime() = runBlockingTest {
        val timer = Timer("timer", 16200000, ListType.DETAIL_LAYOUT, NotificationType.VIBRATION,
            false, "preset1\n150???\n??????: 10??????\tpreset2\n120???\n??????: ??????\t")
        val preset1 = PresetTimer("timer", "preset1", 1, 9000000, 600000)
        val preset2 = PresetTimer("timer", "preset2", 2, 7200000)
        timerRepository.insertTimerAndPresetTimers(timer, listOf(preset1, preset2))
        val bundle = SetTimerFragmentArgs(null, "timer").toBundle()
        launchFragmentInContainer<SetTimerFragment>(bundle, R.style.Theme_TimerApp)

        onView(withId(R.id.pre_notification)).perform(click())
        onView(withId(R.id.numberPickerMin)).perform(clickBottomCentre)
        onView(withId(R.id.numberPickerMin)).perform(clickBottomCentre)
        onView(withId(R.id.numberPickerMin)).perform(clickBottomCentre)
        onView(withId(R.id.numberPickerMin)).perform(clickBottomCentre)
        onView(withId(R.id.numberPickerMin)).perform(clickBottomCentre)
        onView(withId(R.id.numberPickerSec)).perform(clickBottomCentre)
        onView(withText("??????")).perform(click())

        onView(withId(R.id.setting_preset)).check(matches(isDisplayed()))
        onView(withId(R.id.setting_preset)).check(matches(withText("??????: 5???1??? ???")))
    }

    @Test
    fun setPreNotificationTimeZero_notDisplayPreNotificationTime() = runBlockingTest {
        val timer = Timer("timer", 16200000, ListType.DETAIL_LAYOUT, NotificationType.VIBRATION,
            false, "preset1\n150???\n??????: 10??????\tpreset2\n120???\n??????: ??????\t")
        val preset1 = PresetTimer("timer", "preset1", 1, 9000000, 600000)
        val preset2 = PresetTimer("timer", "preset2", 2, 7200000)
        timerRepository.insertTimerAndPresetTimers(timer, listOf(preset1, preset2))
        val bundle = SetTimerFragmentArgs(preset1.presetTimerId, "timer").toBundle()
        launchFragmentInContainer<SetTimerFragment>(bundle, R.style.Theme_TimerApp)

        onView(withId(R.id.pre_notification)).perform(click())
        onView(withId(R.id.numberPickerMin)).perform(clickTopCentre)
        onView(withId(R.id.numberPickerMin)).perform(clickTopCentre)
        onView(withId(R.id.numberPickerMin)).perform(clickTopCentre)
        onView(withId(R.id.numberPickerMin)).perform(clickTopCentre)
        onView(withId(R.id.numberPickerMin)).perform(clickTopCentre)
        onView(withId(R.id.numberPickerMin)).perform(clickTopCentre)
        onView(withId(R.id.numberPickerMin)).perform(clickTopCentre)
        onView(withId(R.id.numberPickerMin)).perform(clickTopCentre)
        onView(withId(R.id.numberPickerMin)).perform(clickTopCentre)
        onView(withId(R.id.numberPickerMin)).perform(clickTopCentre)
        onView(withText("??????")).perform(click())

        onView(withId(R.id.setting_preset)).check(matches(withText("")))
    }

    @Test
    fun clickToBackButton_setNavigateToPresetTimerListEvent() = runBlockingTest {
        val timer = Timer("timer", 16200000, ListType.DETAIL_LAYOUT, NotificationType.VIBRATION,
            false, "preset1\n150???\n??????: 10??????\tpreset2\n120???\n??????: ??????\t")
        val preset1 = PresetTimer("timer", "preset1", 1, 9000000, 600000)
        val preset2 = PresetTimer("timer", "preset2", 2, 7200000)
        timerRepository.insertTimerAndPresetTimers(timer, listOf(preset1, preset2))
        val bundle = SetTimerFragmentArgs(preset1.presetTimerId, "timer").toBundle()
        val navController = Mockito.mock(NavController::class.java)
        val scenario = launchFragmentInContainer<SetTimerFragment>(bundle, R.style.Theme_TimerApp)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        onView(withId(R.id.back_btn)).perform(click())
        verify(navController).popBackStack()
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