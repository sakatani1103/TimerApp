package com.example.timerapp.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.timerapp.MainCoroutineRule
import com.example.timerapp.database.Timer
import com.example.timerapp.getOrAwaitValue
import com.example.timerapp.others.Status
import com.example.timerapp.repository.FakeTimerRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi

import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class TimerListViewModelTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var timerListViewModel: TimerListViewModel
    private lateinit var timerRepository: FakeTimerRepository

    @Before
    fun setup() {
        timerRepository = FakeTimerRepository()
    }

    // タイマーの登録
    // 以下はエラー表示(このfunctionはnameのみの入力)
    // 空の入力
    // 15文字以内
    // 既に同じタイマーの名前が登録されている
    // タイマー数が15個登録されている
    @Test
    fun `insert timer item with empty name, return error`() {
        timerListViewModel = TimerListViewModel(timerRepository)
        timerListViewModel.insertTimer("")
        val value = timerListViewModel.insertTimerItemStatus.getOrAwaitValue()
        assertThat(value.status).isEqualTo(Status.ERROR)
    }

    @Test
    fun `insert timer item with too long name, return error`() {
        timerListViewModel = TimerListViewModel(timerRepository)
        timerListViewModel.insertTimer("VeryVeryVeryLongNameTimer")
        val value = timerListViewModel.insertTimerItemStatus.getOrAwaitValue()
        assertThat(value.status).isEqualTo(Status.ERROR)
    }

    @Test
    fun `insert timer item with registered name, return error`() {
        val timer1 = Timer("timer1")
        val timer2 = Timer("timer2")
        timerRepository.addTasks(timer1, timer2)
        timerListViewModel = TimerListViewModel(timerRepository)
        timerListViewModel.insertTimer("timer1")
        val value = timerListViewModel.insertTimerItemStatus.getOrAwaitValue()
        assertThat(value.status).isEqualTo(Status.ERROR)
    }

    @Test
    fun `despite many timers, insert timer item, return error()`(){
        val timer1 = Timer("timer1")
        val timer2 = Timer("timer2")
        val timer3 = Timer("timer3")
        val timer4 = Timer("timer4")
        val timer5 = Timer("timer5")
        val timer6 = Timer("timer6")
        val timer7 = Timer("timer7")
        val timer8 = Timer("timer8")
        val timer9 = Timer("timer9")
        val timer10 = Timer("timer10")
        val timer11 = Timer("timer11")
        val timer12 = Timer("timer12")
        val timer13 = Timer("timer13")
        val timer14 = Timer("timer14")
        val timer15 = Timer("timer15")
        timerRepository.addTasks(timer1, timer2, timer3, timer4, timer5, timer6,
        timer7, timer8, timer9,  timer10, timer11, timer12, timer13, timer14, timer15)
        timerListViewModel = TimerListViewModel(timerRepository)
        timerListViewModel.insertTimer("timer16")
        val value = timerListViewModel.insertTimerItemStatus.getOrAwaitValue()
        assertThat(value.status).isEqualTo(Status.ERROR)
    }

    @Test
    fun `insert timer item with valid input, return success`() {
        timerListViewModel = TimerListViewModel(timerRepository)
        timerListViewModel.insertTimer("timer")
        val value = timerListViewModel.insertTimerItemStatus.getOrAwaitValue()
        assertThat(value.status).isEqualTo(Status.SUCCESS)
    }


}