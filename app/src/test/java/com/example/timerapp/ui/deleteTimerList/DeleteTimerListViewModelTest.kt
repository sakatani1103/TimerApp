package com.example.timerapp.ui.deleteTimerList

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.timerapp.database.ListType
import com.example.timerapp.database.NotificationType
import com.example.timerapp.database.PresetTimer
import com.example.timerapp.database.Timer
import com.example.timerapp.getOrAwaitValue
import com.example.timerapp.others.Status
import com.example.timerapp.repository.FakeTimerRepository
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class DeleteTimerListViewModelTest {

    private lateinit var timerRepository: FakeTimerRepository
    private lateinit var deleteTimerListViewModel: DeleteTimerListViewModel

    private val timer1 = Timer("timer1")
    private val timer2 = Timer("timer2", 900000, ListType.SIMPLE_LAYOUT, NotificationType.VIBRATION, false,
        "通知: なし")

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setupViewModel() {
        val timer3 = Timer("timer3")
        val presetTimer = PresetTimer("timer2", "preset", 1, 900000)
        timerRepository = FakeTimerRepository()
        timerRepository.addTimers(timer1, timer2, timer3)
        timerRepository.addPresetTimers(presetTimer)
        deleteTimerListViewModel = DeleteTimerListViewModel(timerRepository)
    }

    @Test
    fun deleteSelectedTimers_returnSuccess() {
        deleteTimerListViewModel.switchTimerIsSelected(timer1)
        deleteTimerListViewModel.switchTimerIsSelected(timer2)
        deleteTimerListViewModel.deleteTimerListAndPresetTimerList()

        val deleteTimerItemStatus = deleteTimerListViewModel.deleteTimerItemStatus.getOrAwaitValue().getContentIfNotHandled()
        assertThat(deleteTimerItemStatus?.status).isEqualTo(Status.SUCCESS)
        assertThat(deleteTimerItemStatus?.data).isEqualTo(mutableListOf(timer1, timer2))
    }

    @Test
    fun cancelDelete_returnSuccess() {
        deleteTimerListViewModel.switchTimerIsSelected(timer1)
        deleteTimerListViewModel.switchTimerIsSelected(timer2)
        deleteTimerListViewModel.cancelDeleteTimerList()

        val deleteTimerItemStatus = deleteTimerListViewModel.deleteTimerItemStatus.getOrAwaitValue().getContentIfNotHandled()
        assertThat(deleteTimerItemStatus?.status).isEqualTo(Status.SUCCESS)
    }
}