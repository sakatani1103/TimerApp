package com.example.timerapp.database

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.example.timerapp.getOrAwaitValueTest
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class TimerDaoTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: TimerDatabase
    private lateinit var dao: TimerDao

    @Before
    fun initDb() {
        database = Room.inMemoryDatabaseBuilder(
            getApplicationContext(),
            TimerDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.timerDao()
    }

    @After
    fun closeDb() = database.close()

    @Test
    fun insertTimer() = runBlockingTest {
        val timer = Timer("timer")
        dao.insertTimer(timer)

        val allTimer = dao.observeAllTimer().getOrAwaitValueTest()
        assertThat(allTimer).contains(timer)
    }

    @Test
    fun insertPresetTimer() = runBlockingTest {
        val preset = PresetTimer("timer", "preset", 1, 600000)
        dao.insertPresetTimer(preset)

        val allPresets = dao.observeAllPresetTimer().getOrAwaitValueTest()
        assertThat(allPresets).contains(preset)
    }

    @Test
    fun insertPresetTimers() = runBlockingTest {
        val preset1 = PresetTimer("timer", "preset1", 1, 600000)
        val preset2 = PresetTimer("timer", "preset2", 2, 900000)
        val preset3 = PresetTimer("timer", "preset3", 3, 720000)
        dao.insertPresetTimers(listOf(preset1, preset2, preset3))

        val allPresets = dao.observeAllPresetTimer().getOrAwaitValueTest()
        assertThat(allPresets).isEqualTo(listOf(preset1, preset2, preset3))
    }

    @Test
    fun insertTimerAndPresetTimers() = runBlockingTest {
        val timer = Timer("timer")
        val preset1 = PresetTimer("timer", "preset1", 1, 600000)
        val preset2 = PresetTimer("timer", "preset2", 2, 900000)
        val preset3 = PresetTimer("timer", "preset3", 3, 720000)
        dao.insertTimerAndPresetTimers(timer, listOf(preset1, preset2, preset3))
    }

    @Test
    fun updateTimer() = runBlockingTest {
        val timer = Timer("timer", 9000000, ListType.SIMPLE_LAYOUT, NotificationType.VIBRATION)
        dao.insertTimer(timer)
        val updateTimer = Timer("updateTimer", 6000000, ListType.DETAIL_LAYOUT, NotificationType.ALARM,
        false, timer.detail, false, timer.timerId)
        dao.updateTimer(updateTimer)

        val allTimer = dao.observeAllTimer().getOrAwaitValueTest()
        assertThat(allTimer).doesNotContain(timer)
        assertThat(allTimer).contains(updateTimer)
    }

    @Test
    fun updateTimers() = runBlockingTest {
        val timer1 = Timer("timer1", 9000000, ListType.SIMPLE_LAYOUT, NotificationType.VIBRATION)
        val timer2 = Timer("timer2", 6000000, ListType.DETAIL_LAYOUT, NotificationType.ALARM)
        dao.insertTimer(timer1)
        dao.insertTimer(timer2)

        val updateTimer1 = Timer("updateTimer1", 6000000, ListType.SIMPLE_LAYOUT, NotificationType.ALARM, false, timer1.detail, false, timer1.timerId)
        val updateTimer2 = Timer("updateTimer2", 9000000, ListType.DETAIL_LAYOUT, NotificationType.ALARM, false, timer2.detail, false, timer2.timerId)
        dao.updateTimers(listOf(updateTimer1, updateTimer2))

        val allTimer = dao.observeAllTimer().getOrAwaitValueTest()
        assertThat(allTimer).doesNotContain(timer1)
        assertThat(allTimer).doesNotContain(timer2)
        assertThat(allTimer).contains(updateTimer1)
        assertThat(allTimer).contains(updateTimer2)
    }

    @Test
    fun updatePresetTimer() = runBlockingTest {
        val preset = PresetTimer("timer", "preset", 1, 600000)
        dao.insertPresetTimer(preset)

        val updatePreset = PresetTimer("timer", "updatePreset", 2, 900000, 0, false, preset.presetTimerId)
        dao.updatePresetTimer(updatePreset)

        val allPresets = dao.observeAllPresetTimer().getOrAwaitValueTest()
        assertThat(allPresets).doesNotContain(preset)
        assertThat(allPresets).contains(updatePreset)
    }

    @Test
    fun updatePresetTimers() = runBlockingTest {
        val preset1 = PresetTimer("timer", "preset1", 1, 600000)
        val preset2 = PresetTimer("timer", "preset2", 2, 600000)
        val preset3 = PresetTimer("timer", "preset3", 3, 600000)
        dao.insertPresetTimers(listOf(preset1, preset2, preset3))

        val updatePreset1 = PresetTimer("timer", "updatePreset1", 2, 900000, 0, false, preset1.presetTimerId)
        val updatePreset2 = PresetTimer("timer", "updatePreset2", 1, 720000, 60000, false, preset2.presetTimerId)
        val updatePreset3 = PresetTimer("timer", "updatePreset3", 3, 600000, 0, false, preset3.presetTimerId)
        dao.updatePresetTimers(listOf(updatePreset1, updatePreset2, updatePreset3))

        val allPresets = dao.observeAllPresetTimer().getOrAwaitValueTest()
        assertThat(allPresets).isEqualTo(listOf(updatePreset2, updatePreset1, updatePreset3))
    }

    @Test
    fun updateTimerAndPresetTimers() = runBlockingTest {
        val timer = Timer("timer", 9000000, ListType.SIMPLE_LAYOUT, NotificationType.VIBRATION)
        val preset1 = PresetTimer("timer", "preset1", 1, 600000)
        val preset2 = PresetTimer("timer", "preset2", 2, 600000)
        val preset3 = PresetTimer("timer", "preset3", 3, 600000)
        dao.insertTimerAndPresetTimers(timer, listOf(preset1, preset2, preset3))

        val updateTimer = Timer("updateTimer", 6000000, ListType.DETAIL_LAYOUT, NotificationType.ALARM,
            false, timer.detail, false, timer.timerId)
        val updatePreset1 = PresetTimer("timer", "updatePreset1", 2, 900000, 0, false, preset1.presetTimerId)
        val updatePreset2 = PresetTimer("timer", "updatePreset2", 1, 720000, 60000, false, preset2.presetTimerId)
        val updatePreset3 = PresetTimer("timer", "updatePreset3", 3, 600000, 0, false, preset3.presetTimerId)
        dao.updateTimerAndPresetTimers(updateTimer, listOf(updatePreset1, updatePreset2, updatePreset3))

        val allTimer = dao.observeAllTimer().getOrAwaitValueTest()
        assertThat(allTimer).doesNotContain(timer)
        assertThat(allTimer).contains(updateTimer)

        val allPresets = dao.observeAllPresetTimer().getOrAwaitValueTest()
        assertThat(allPresets).isEqualTo(listOf(updatePreset2, updatePreset1, updatePreset3))
    }

    @Test
    fun deleteTimer() = runBlockingTest {
        val timer1 = Timer("timer1", 9000000, ListType.SIMPLE_LAYOUT, NotificationType.VIBRATION)
        val timer2 = Timer("timer2", 6000000, ListType.DETAIL_LAYOUT, NotificationType.ALARM)
        dao.insertTimer(timer1)
        dao.insertTimer(timer2)

        dao.deleteTimer(timer1)
        val allTimer = dao.observeAllTimer().getOrAwaitValueTest()
        assertThat(allTimer).doesNotContain(timer1)
    }

    @Test
    fun deletePresetTimer() = runBlockingTest {
        val preset1 = PresetTimer("timer", "preset1", 1, 600000)
        val preset2 = PresetTimer("timer", "preset2", 2, 600000)
        val preset3 = PresetTimer("timer", "preset3", 3, 600000)
        dao.insertPresetTimers(listOf(preset1, preset2, preset3))

        dao.deletePresetTimer(preset1)
        val allPresets = dao.observeAllPresetTimer().getOrAwaitValueTest()
        assertThat(allPresets).doesNotContain(preset1)
    }

    @Test
    fun deletePresetTimers() = runBlockingTest {
        val preset1 = PresetTimer("timer", "preset1", 1, 600000)
        val preset2 = PresetTimer("timer", "preset2", 2, 600000)
        val preset3 = PresetTimer("timer", "preset3", 3, 600000)
        dao.insertPresetTimers(listOf(preset1, preset2, preset3))

        dao.deletePresetTimers(listOf(preset1,preset2))
        val allPresets = dao.observeAllPresetTimer().getOrAwaitValueTest()
        assertThat(allPresets).doesNotContain(preset1)
        assertThat(allPresets).doesNotContain(preset2)
    }

    @Test
    fun deleteTimerAndPresetTimers() = runBlockingTest {
        val timer = Timer("timer", 9000000, ListType.SIMPLE_LAYOUT, NotificationType.VIBRATION)
        val preset1 = PresetTimer("timer", "preset1", 1, 600000)
        val preset2 = PresetTimer("timer", "preset2", 2, 600000)
        val preset3 = PresetTimer("timer", "preset3", 3, 600000)
        dao.insertTimerAndPresetTimers(timer, listOf(preset1, preset2, preset3))

        dao.deleteTimerAndPresetTimers(timer, listOf(preset1, preset2, preset3))
        val allPresets = dao.observeAllPresetTimer().getOrAwaitValueTest()
        assertThat(allPresets).isEmpty()
    }

    @Test
    fun getPresetTimersAndTimer() = runBlockingTest {
        val timer = Timer("timer", 9000000, ListType.SIMPLE_LAYOUT, NotificationType.VIBRATION)
        val preset1 = PresetTimer("timer", "preset1", 1, 600000)
        val preset2 = PresetTimer("timer", "preset2", 2, 600000)
        val preset3 = PresetTimer("timer", "preset3", 3, 600000)
        dao.insertTimerAndPresetTimers(timer, listOf(preset1, preset2, preset3))

        val timerWithPresetTimer = dao.getPresetTimerWithTimer("timer")
        assertThat(timerWithPresetTimer.timer).isEqualTo(timer)
        assertThat(timerWithPresetTimer.presetTimer).isEqualTo(listOf(preset1, preset2, preset3))
    }

    @Test
    fun getCurrentPresetTimer() = runBlockingTest {
        val preset1 = PresetTimer("timer", "preset1", 1, 600000)
        val preset2 = PresetTimer("timer", "preset2", 2, 600000)
        val preset3 = PresetTimer("timer", "preset3", 3, 600000)
        dao.insertPresetTimers(listOf(preset1, preset2, preset3))

        val getValue = dao.getCurrentPresetTimer(preset1.presetTimerId)
        assertThat(getValue).isEqualTo(preset1)
    }

    @Test
    fun getTimerNames() = runBlockingTest {
        val timer1 = Timer("timer1", 9000000, ListType.SIMPLE_LAYOUT, NotificationType.VIBRATION)
        val timer2 = Timer("timer2", 6000000, ListType.DETAIL_LAYOUT, NotificationType.ALARM)
        val timer3 = Timer("timer3", 9000000, ListType.SIMPLE_LAYOUT, NotificationType.VIBRATION)
        dao.insertTimer(timer1)
        dao.insertTimer(timer2)
        dao.insertTimer(timer3)

        val getValue = dao.getTimerNames()
        assertThat(getValue).isEqualTo(listOf("timer1", "timer2", "timer3"))
    }

}