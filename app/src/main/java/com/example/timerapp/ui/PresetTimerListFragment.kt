package com.example.timerapp.ui

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.timerapp.R
import com.example.timerapp.adapter.PresetTimerListAdapter
import com.example.timerapp.database.Timer
import com.example.timerapp.databinding.FragmentPresetTimerListBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PresetTimerListFragment : Fragment() {
    val args: PresetTimerListFragmentArgs by navArgs()
    private lateinit var viewModel: TimerViewModel

    private lateinit var presetTimerListAdapter: PresetTimerListAdapter

    private var _binding: FragmentPresetTimerListBinding? = null
    private val binding: FragmentPresetTimerListBinding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_preset_timer_list, container, false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[TimerViewModel::class.java]
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        viewModel.getCurrentTimer(args.name)

        subscribeToPresetTimerListObservers()
        setupRecyclerView()

        binding.addList.setOnClickListener {
            viewModel.getCurrentTimer(args.name)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupRecyclerView() {
        presetTimerListAdapter =
            PresetTimerListAdapter(viewLifecycleOwner)
        binding.presetTimerList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = presetTimerListAdapter
        }
    }

    private fun subscribeToPresetTimerListObservers() {
        viewModel.presetTimerList.observe(viewLifecycleOwner, Observer {
            presetTimerListAdapter.presetTimerItems = it
        })
        viewModel.currentTimer.observe(viewLifecycleOwner, Observer {
            presetTimerListAdapter.setCurrentTimer(it)
        })

    }
}
