package com.example.timerapp.database

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.example.timerapp.getOrAwaitValueTest
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@SmallTest
@RunWith(AndroidJUnit4::class)
class TimerDaoTest {

    private lateinit var database: TimerDatabase
    private lateinit var dao: TimerDao

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            TimerDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.timerDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertTimer() = runBlockingTest {
        val timerItem1 = Timer("test1") // 最初はtimer名のみを入力
        val timerItem2 = Timer("test2")
        val timerItem3 = Timer("test3")
        dao.insertTimer(timerItem1)
        dao.insertTimer(timerItem2)
        dao.insertTimer(timerItem3)

        val allTimerItems = dao.observeAllTimer().getOrAwaitValueTest()
        assertThat(allTimerItems).contains(timerItem1)
        assertThat(allTimerItems).contains(timerItem2)
        assertThat(allTimerItems).contains(timerItem3)
    }

    @Test
    fun insertPresetTimer() = runBlockingTest {
        val presetTimer = PresetTimer("test1","preset1", 1,6000000, 0)
        dao.insertPresetTimer(presetTimer)

        val allPresetTimer = dao.observeAllPresetTimer().getOrAwaitValueTest()
        assertThat(allPresetTimer).contains(presetTimer)
    }

    @Test
    fun insertPresetTimers() = runBlockingTest {
        val presetTimer1 = PresetTimer("test1","preset1", 1,6000000, 0)
        val presetTimer2 = PresetTimer("test1","preset1", 2,6000000, 0)
        dao.insertPresetTimers(listOf(presetTimer1, presetTimer2))

        val allPresetTimer = dao.observeAllPresetTimer().getOrAwaitValueTest()
        assertThat(allPresetTimer).isEqualTo(listOf(presetTimer1, presetTimer2))
    }

    @Test
    fun insertTimerAndPresetTimers() = runBlockingTest {
        val timerItem = Timer("test1")
        val presetTimer1 = PresetTimer("test1","preset1", 1,6000000, 0)
        val presetTimer2 = PresetTimer("test1","preset2", 2,6000000,0)
        dao.insertTimerAndPresetTimers(timerItem, listOf(presetTimer1, presetTimer2))
        val allTimerItems = dao.observeAllTimer().getOrAwaitValueTest()
        assertThat(allTimerItems).contains(timerItem)
        val allPresetTimer = dao.observeAllPresetTimer().getOrAwaitValueTest()
        assertThat(allPresetTimer).contains(presetTimer1)
    }

    @Test
    fun updateTimer() = runBlockingTest {
        val timerItem1 = Timer("test1")
        dao.insertTimer(timerItem1)
        val timerItem2 = Timer("test1", 0, ListType.DETAIL_LAYOUT, NotificationType.ALARM)
        dao.updateTimer(timerItem2)

        val allTimerItems = dao.observeAllTimer().getOrAwaitValueTest()
        assertThat(allTimerItems).doesNotContain(timerItem1)
        assertThat(allTimerItems).contains(timerItem2)
    }

    @Test
    fun updateTimers() = runBlockingTest {
        val timerItem1 = Timer("test1")
        val timerItem2 = Timer("test2")
        dao.insertTimer(timerItem1)
        dao.insertTimer(timerItem2)

        val timerItem3 = Timer("test1", 0, ListType.DETAIL_LAYOUT, NotificationType.ALARM)
        val timerItem4 = Timer("test2", 0, ListType.DETAIL_LAYOUT, NotificationType.ALARM)
        dao.updateTimers(listOf(timerItem3, timerItem4))
        val allTimerItems = dao.observeAllTimer().getOrAwaitValueTest()
        assertThat(allTimerItems).doesNotContain(timerItem1)
        assertThat(allTimerItems).contains(timerItem3)
    }

    @Test
    fun updatePresetTimers() = runBlockingTest {
        val presetTimerItem1 = PresetTimer("test1", "preset1", 1,
            6000000, 0)
        val presetTimerItem2 = PresetTimer("test1", "preset2", 2,
            6000000, 0)
        dao.insertPresetTimer(presetTimerItem1)
        dao.insertPresetTimer(presetTimerItem2)
        val presetTimerItem3 = PresetTimer("test1", "preset1", 1,
            10000000, 600000)
        val presetTimerItem4 = PresetTimer("test1", "preset2", 2,
            9000000, 0)
        dao.updatePresetTimers(listOf(presetTimerItem3,presetTimerItem4))

        val allTimerItems = dao.observeAllPresetTimer().getOrAwaitValueTest()
        assertThat(allTimerItems).doesNotContain(presetTimerItem1)
        assertThat(allTimerItems).contains(presetTimerItem3)
    }

    @Test
    fun deleteTimer() = runBlockingTest {
        val timerItem = Timer("test1")
        dao.insertTimer(timerItem)
        dao.deleteTimer(timerItem)

        val allTimerItems = dao.observeAllTimer().getOrAwaitValueTest()
        assertThat(allTimerItems).doesNotContain(timerItem)
    }

    @Test
    fun deletePresetTimer() = runBlockingTest {
        val presetTimer = PresetTimer("test1","preset1", 1,6000000, 0)
        dao.insertPresetTimer(presetTimer)
        dao.deletePresetTimer(presetTimer)

        val allPresetTimer = dao.observeAllPresetTimer().getOrAwaitValueTest()
        assertThat(allPresetTimer).doesNotContain(presetTimer)
    }

    @Test
    fun deletePresetTimers() = runBlockingTest {
        val presetTimer1 = PresetTimer("test1","preset1", 1,6000000, 0)
        val presetTimer2 = PresetTimer("test1","preset2", 2,6000000, 0)
        dao.insertPresetTimer(presetTimer1)
        dao.insertPresetTimer(presetTimer2)
        dao.deletePresetTimers(listOf(presetTimer1, presetTimer2))
        val allPresetTimer = dao.observeAllPresetTimer().getOrAwaitValueTest()
        assertThat(allPresetTimer).doesNotContain(presetTimer1)
    }

    @Test
    fun deleteTimerAndPresetTimers() = runBlockingTest {
        val timer = Timer("test1")
        dao.insertTimer(timer)
        val presetTimer1 = PresetTimer("test1","preset1", 1,6000000, 0)
        val presetTimer2 = PresetTimer("test1","preset2", 2,6000000, 0)
        dao.insertPresetTimer(presetTimer1)
        dao.insertPresetTimer(presetTimer2)
        dao.deleteTimerAndPresetTimers(timer, listOf(presetTimer1, presetTimer2))

        val allTimerItems = dao.observeAllTimer().getOrAwaitValueTest()
        assertThat(allTimerItems).doesNotContain(timer)
        val allPresetTimers = dao.observeAllPresetTimer().getOrAwaitValueTest()
        assertThat(allPresetTimers).doesNotContain(presetTimer1)
    }

    @Test
    fun getPresetTimerWithTimer() = runBlockingTest{
        val timerItem1 = Timer("test1")
        dao.insertTimer(timerItem1)

        val presetTimerItem1 = PresetTimer("test1", "preset1", 1, 6000000, 0)
        val presetTimerItem2 = PresetTimer("test1", "preset2", 2, 9000000, 600000)
        val presetTimerItem3 = PresetTimer("test2", "preset1", 3, 6000000, 0)
        dao.insertPresetTimer(presetTimerItem1)
        dao.insertPresetTimer(presetTimerItem2)
        dao.insertPresetTimer(presetTimerItem3)

        val timerWithPresetTimer = dao.getPresetTimerWithTimer("test1")
        val test1PresetTimer = timerWithPresetTimer.presetTimer[0]
        assertThat(test1PresetTimer).isEqualTo(presetTimerItem1)
        val test2PresetTimer = timerWithPresetTimer.presetTimer[1]
        assertThat(test2PresetTimer).isEqualTo(presetTimerItem2)
    }

    @Test
    fun getCurrentTimer() = runBlockingTest {
        val timerItem1 = Timer("test1")
        val timerItem2 = Timer("test2")
        val timerItem3 = Timer("test3")
        dao.insertTimer(timerItem1)
        dao.insertTimer(timerItem2)
        dao.insertTimer(timerItem3)

        val allCurrentTimer = dao.getCurrentTimer("test1")

        assertThat(allCurrentTimer).isEqualTo(timerItem1)
    }

    @Test
    fun getCurrentPresetTimer() = runBlockingTest {
        val presetTimer1 = PresetTimer("test1", "presetTimer1", 1,
            6000000, 600000)
        val presetTimer2 = PresetTimer("test1", "presetTimer2", 2,
            9000000, 0)
        val presetTimer3 = PresetTimer("test1", "presetTimer3", 3,
            6000000, 0)
        dao.insertPresetTimer(presetTimer1)
        dao.insertPresetTimer(presetTimer2)
        dao.insertPresetTimer(presetTimer3)

        val currentPresetTimer = dao.getCurrentPresetTimer("test1","presetTimer1",1)
        assertThat(currentPresetTimer).isEqualTo(presetTimer1)
    }

    @Test
    fun getMaxOrderPresetTimer() = runBlockingTest {
        val timerItem1 = Timer("test1")
        val presetTimer1 = PresetTimer("test1", "presetTimer1", 1,
            6000000, 600000)
        val presetTimer2 = PresetTimer("test1", "presetTimer2", 2,
            9000000, 0)
        dao.insertTimer(timerItem1)
        dao.insertPresetTimer(presetTimer1)
        dao.insertPresetTimer(presetTimer2)

        val order = dao.getMaxOrderPresetTimer("test1")
        assertThat(order).isEqualTo(2)
    }

}