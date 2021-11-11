package com.example.timerapp.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.timerapp.MainCoroutineRule
import com.example.timerapp.database.Timer
import com.example.timerapp.getOrAwaitValue
import com.example.timerapp.others.Status
import com.example.timerapp.repository.FakeTimerRepository
import com.google.common.truth.Truth
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class TimerViewModelUpdateTest {
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

    // タイマー名の変更 下記の場合にERR
    // 空の入力
    // 15文字以内
    // 既に同じタイマーの名前が登録されている(変更なしの場合はスルー)
    @Test
    fun `update timer item with empty name, return error`(){
        timerViewModel.updateTimerName("")
        val value = timerViewModel.updatePresetTimerItemStatus.getOrAwaitValue()
        Truth.assertThat(value.status).isEqualTo(Status.ERROR)
    }

    @Test
    fun `update timer item with too long name, return error`() {
        timerViewModel.updateTimerName("VeryVeryVeryLongNameTimer")
        val value = timerViewModel.updatePresetTimerItemStatus.getOrAwaitValue()
        Truth.assertThat(value.status).isEqualTo(Status.ERROR)
    }

    @Test
    fun `update timer item with registered name, return error`() {
        timerViewModel.updateTimerName("timer2")
        val value = timerViewModel.updatePresetTimerItemStatus.getOrAwaitValue()
        Truth.assertThat(value.status).isEqualTo(Status.ERROR)
    }

    @Test
    fun `update timer item with valid input, return success`() {
        timerViewModel.updateTimerName("timer")
        val value = timerViewModel.updatePresetTimerItemStatus.getOrAwaitValue()
        Truth.assertThat(value.status).isEqualTo(Status.SUCCESS)
    }

    @Test
    fun `update timer item with no changed input, return success`() {
        timerViewModel.updateTimerName("timer1")
        val value = timerViewModel.updatePresetTimerItemStatus.getOrAwaitValue()
        Truth.assertThat(value.status).isEqualTo(Status.SUCCESS)
    }
}