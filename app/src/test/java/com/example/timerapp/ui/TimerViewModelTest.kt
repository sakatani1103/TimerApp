package com.example.timerapp.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.timerapp.MainCoroutineRule
import com.example.timerapp.database.Timer
import com.example.timerapp.getOrAwaitValue
import com.example.timerapp.others.Status
import com.example.timerapp.repository.FakeTimerRepository
import com.google.common.truth.Truth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class TimerViewModelTest {
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var timerViewModel: TimerViewModel
    private lateinit var timerRepository: FakeTimerRepository

    @Before
    fun setup() {
        timerRepository = FakeTimerRepository()
        val timer1 = Timer("timer1")
        val timer2 = Timer("timer2")
        timerRepository.addTasks(timer1, timer2)
        timerViewModel = TimerViewModel(timerRepository)
        val name = "timer1"
        timerViewModel.getCurrentTimer(name)
    }

    // InsertTest insert
    // タイマーの登録
    // 以下はエラー表示(このfunctionはnameのみの入力)
    // 空の入力
    // 15文字以内
    // 既に同じタイマーの名前が登録されている
    // タイマー数が15個登録されている
    @Test
    fun `insert timer item with empty name, return error`() {
        timerViewModel.insertTimer("")
        val value = timerViewModel.timerNameStatus.getOrAwaitValue()
        Truth.assertThat(value.getContentIfNotHandled()?.status).isEqualTo(Status.ERROR)
    }

    @Test
    fun `insert timer item with too long name, return error`() {
        timerViewModel.insertTimer("VeryVeryVeryLongNameTimer")
        val value = timerViewModel.timerNameStatus.getOrAwaitValue()
        Truth.assertThat(value.getContentIfNotHandled()?.status).isEqualTo(Status.ERROR)
    }

    @Test
    fun `insert timer item with registered name, return error`() {
        timerViewModel.insertTimer("timer1")
        val value = timerViewModel.timerNameStatus.getOrAwaitValue()
        Truth.assertThat(value.getContentIfNotHandled()?.status).isEqualTo(Status.ERROR)
    }

    @Test
    fun `despite many timers, insert timer item, return error()`(){
        timerViewModel.insertTimer("timer3")
        timerViewModel.insertTimer("timer4")
        timerViewModel.insertTimer("timer5")
        timerViewModel.insertTimer("timer6")
        timerViewModel.insertTimer("timer7")
        timerViewModel.insertTimer("timer8")
        timerViewModel.insertTimer("timer9")
        timerViewModel.insertTimer("timer10")
        timerViewModel.insertTimer("timer11")
        timerViewModel.insertTimer("timer12")
        timerViewModel.insertTimer("timer13")
        timerViewModel.insertTimer("timer14")
        timerViewModel.insertTimer("timer15")
        timerViewModel.insertTimer("timer16")
        val value = timerViewModel.timerNameStatus.getOrAwaitValue()
        Truth.assertThat(value.getContentIfNotHandled()?.status).isEqualTo(Status.ERROR)
    }

    @Test
    fun `insert timer item with valid input, return success`() {
        timerViewModel.insertTimer("timer")
        val value = timerViewModel.timerNameStatus.getOrAwaitValue()
        Truth.assertThat(value.getContentIfNotHandled()?.status).isEqualTo(Status.SUCCESS)
    }


    // タイマー名の変更 下記の場合にERR
    // 空の入力
    // 15文字以内
    // 既に同じタイマーの名前が登録されている(変更なしの場合はスルー)
    @Test
    fun `update timer item with empty name, return error`(){
        timerViewModel.updateTimerName("")
        val value = timerViewModel.timerItemStatus.getOrAwaitValue()
        Truth.assertThat(value.status).isEqualTo(Status.ERROR)
    }

    @Test
    fun `update timer item with too long name, return error`() {
        timerViewModel.updateTimerName("VeryVeryVeryLongNameTimer")
        val value = timerViewModel.timerItemStatus.getOrAwaitValue()
        Truth.assertThat(value.status).isEqualTo(Status.ERROR)
    }

    @Test
    fun `update timer item with registered name, return error`() {
        timerViewModel.updateTimerName("timer2")
        val value = timerViewModel.timerItemStatus.getOrAwaitValue()
        Truth.assertThat(value.status).isEqualTo(Status.ERROR)
    }

    @Test
    fun `update timer item with valid input, return success`() {
        timerViewModel.updateTimerName("timer")
        val value = timerViewModel.timerItemStatus.getOrAwaitValue()
        Truth.assertThat(value.status).isEqualTo(Status.SUCCESS)
    }

    @Test
    fun `update timer item with no changed input, return success`() {
        timerViewModel.updateTimerName("timer1")
        val value = timerViewModel.timerItemStatus.getOrAwaitValue()
        Truth.assertThat(value.status).isEqualTo(Status.SUCCESS)
    }
}