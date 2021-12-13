package com.example.timerapp.ui.deletePresetTimerList

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

class DeletePresetTimerListViewModelTest {

    private lateinit var timerRepository: FakeTimerRepository
    private lateinit var deletePresetTimerListViewModel: DeletePresetTimerListViewModel

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private val presetTime1 = PresetTimer("timer", "preset1", 1, 9000000)
    private val presetTime2 = PresetTimer("timer", "preset2", 2, 9000000)
    private val presetTime3 = PresetTimer("timer", "preset3", 3, 9000000)


    @Before
    fun setupViewModel() {
        val timer = Timer("timer", 27000000, ListType.SIMPLE_LAYOUT, NotificationType.VIBRATION, false,
            "preset1\t150分\t通知: なし\npreset2\t150分\t通知: なし\npreset3\t150分\t通知: なし\n")
        timerRepository = FakeTimerRepository()
        timerRepository.addTimers(timer)
        timerRepository.addPresetTimers(presetTime1, presetTime2, presetTime3)
        deletePresetTimerListViewModel = DeletePresetTimerListViewModel(timerRepository)
    }

    @Test
    fun deleteSelectedPresetTimers_returnSuccess() {
        deletePresetTimerListViewModel.start("timer")
        deletePresetTimerListViewModel.switchPresetTimerIsSelected(presetTime1)
        deletePresetTimerListViewModel.switchPresetTimerIsSelected(presetTime2)
        deletePresetTimerListViewModel.deletePresetTimerList()

        val deletePresetTimerItemStatus = deletePresetTimerListViewModel.deletePresetTimerItemStatus.getOrAwaitValue().getContentIfNotHandled()
        assertThat(deletePresetTimerItemStatus?.status).isEqualTo(Status.SUCCESS)
        assertThat(deletePresetTimerItemStatus?.data).isEqualTo(mutableListOf(presetTime1, presetTime2))
    }

    @Test
    fun cancelDelete_returnSuccess() {
        deletePresetTimerListViewModel.start("timer")
        deletePresetTimerListViewModel.switchPresetTimerIsSelected(presetTime1)
        deletePresetTimerListViewModel.switchPresetTimerIsSelected(presetTime2)
        deletePresetTimerListViewModel.cancelDeletePresetTimerList()

        val deletePresetTimerItemStatus = deletePresetTimerListViewModel.deletePresetTimerItemStatus.getOrAwaitValue().getContentIfNotHandled()
        assertThat(deletePresetTimerItemStatus?.status).isEqualTo(Status.SUCCESS)
    }

}