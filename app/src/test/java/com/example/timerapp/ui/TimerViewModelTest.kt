package com.example.timerapp.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.timerapp.MainCoroutineRule
import com.example.timerapp.getOrAwaitValue
import com.example.timerapp.others.Status
import com.example.timerapp.repository.FakeTimerRepository
import com.google.common.truth.Truth.assertThat
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

    @Before
    fun setup() {
        timerViewModel = TimerViewModel(FakeTimerRepository())
    }

    @Test
    fun `insert timer item with empty name, return error`() {
        timerViewModel.insertTimer("")
        val value = timerViewModel.insertTimerItemStatus.getOrAwaitValue()
        assertThat(value.status).isEqualTo(Status.ERROR)
    }


}