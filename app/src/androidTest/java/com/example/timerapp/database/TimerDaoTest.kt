package com.example.timerapp.database

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
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
        val timerItem = Timer("test1") // 最初はtimer名のみを入力
        dao.insertTimer(timerItem)

        val allTimerItems = dao.observeAllTimer().getOrAwaitValueTest()
        assertThat(allTimerItems).contains(timerItem)
    }

    @Test
    fun insertPresetTimer() = runBlockingTest {
        val presetTimer = PresetTimer("test1","preset1", 30,
            10, 1)
        dao.insertPresetTimer(presetTimer)

        val allPresetTimer = dao.observeAllPresetTimer().getOrAwaitValueTest()
        assertThat(allPresetTimer).contains(presetTimer)
    }

    @Test
    fun updateTimer() = runBlockingTest {
        val timerItem1 = Timer("test1")
        dao.insertTimer(timerItem1)
        val timerItem2 = Timer("test1", 40, ListType.DETAIL_LAYOUT, NotificationType.ALARM)
        dao.updateTimer(timerItem2)

        val allTimerItems = dao.observeAllTimer().getOrAwaitValueTest()
        assertThat(allTimerItems).doesNotContain(timerItem1)
        assertThat(allTimerItems).contains(timerItem2)
    }

    @Test
    fun updatePresetTimer() = runBlockingTest {
        val presetTimerItem1 = PresetTimer("test1", "preset1", 40,
            20, 1)
        dao.insertPresetTimer(presetTimerItem1)
        val presetTimerItem2 = PresetTimer("test1", "preset2", 20,
            10, 1)
        dao.updatePresetTimer(presetTimerItem2)

        val allTimerItems = dao.observeAllPresetTimer().getOrAwaitValueTest()
        assertThat(allTimerItems).doesNotContain(presetTimerItem1)
        assertThat(allTimerItems).contains(presetTimerItem2)
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
        val presetTimer = PresetTimer("test1","preset1", 30,10, null)
        dao.insertPresetTimer(presetTimer)
        dao.deletePresetTimer(presetTimer)

        val allPresetTimer = dao.observeAllPresetTimer().getOrAwaitValueTest()
        assertThat(allPresetTimer).doesNotContain(presetTimer)
    }

    @Test
    fun getPresetTimerWithTimer() = runBlockingTest{
        val timerItem1 = Timer("test1")
        dao.insertTimer(timerItem1)

        val presetTimerItem1 = PresetTimer("test1", "preset1", 40, 20, 1)
        val presetTimerItem2 = PresetTimer("test1", "preset2", 30, 20, 2)
        val presetTimerItem3 = PresetTimer("test2", "preset1", 20, 10, 3)
        dao.insertPresetTimer(presetTimerItem1)
        dao.insertPresetTimer(presetTimerItem2)
        dao.insertPresetTimer(presetTimerItem3)

        // TimerWithPresetTimer(
        // timer=Timer(name=test1, total=0, listType=SIMPLE_LAYOUT, notificationType=VIBRATION, isExpanded=false),
        // presetTimer=[PresetTimer(name=test1, presetName=preset1, presetTime=40, notificationTime=20, presetTimerId=1),
        // PresetTimer(name=test1, presetName=preset2, presetTime=30, notificationTime=20, presetTimerId=2)])
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
}