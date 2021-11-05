package com.example.timerapp.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.timerapp.R
import com.example.timerapp.adapter.PresetTimerListAdapter
import com.example.timerapp.database.PresetTimer
import com.example.timerapp.databinding.FragmentPresetTimerListBinding
import com.example.timerapp.databinding.FragmentTimerListBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PresetTimerListFragment : Fragment() {
    lateinit var viewModel: TimerViewModel

    private var _binding: FragmentPresetTimerListBinding? = null
    private val binding: FragmentPresetTimerListBinding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_preset_timer_list, container, false)

        val list: MutableList<PresetTimer> = mutableListOf()
        list.add(
            PresetTimer("基本情報技術者午前試験", "設問1", 20, 5, 1)
        )
        list.add(PresetTimer("基本情報技術者午前試験", "設問2-5", 30, 10, 2))
        list.add(PresetTimer("基本情報技術者午前試験", "設問2-5", 30,  5, 3))
        list.add(PresetTimer("基本情報技術者午前試験", "設問2-5", 30, 10, 4))
        list.add(PresetTimer("基本情報技術者午前試験", "設問2-5", 30, 10, 5))

        setupRecyclerView(list)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[TimerViewModel::class.java]
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding= null
    }

    private fun setupRecyclerView(data: MutableList<PresetTimer>) {
        binding.presetTimerList.apply {
            adapter = PresetTimerListAdapter(data)

        }
    }

}