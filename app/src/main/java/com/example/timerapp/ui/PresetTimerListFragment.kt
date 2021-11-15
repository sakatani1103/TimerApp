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
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.timerapp.R
import com.example.timerapp.adapter.PresetTimerClickListener
import com.example.timerapp.adapter.PresetTimerListAdapter
import com.example.timerapp.database.Timer
import com.example.timerapp.databinding.FragmentPresetTimerListBinding
import com.example.timerapp.others.Constants
import com.google.android.material.snackbar.Snackbar
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

        subscribeToPresetTimerListObservers()
        setupRecyclerView()

        binding.addList.setOnClickListener { addPresetTimer() }
        binding.deleteList.setOnClickListener { deletePresetTimer() }
        binding.backBtn.setOnClickListener { this.findNavController().popBackStack() }
        binding.startBtn.setOnClickListener { startTimer() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupRecyclerView() {
        val clickListener = PresetTimerClickListener { timerName, presetName ->
            viewModel.navigateToSettingTimer(timerName, presetName)
        }
        presetTimerListAdapter =
            PresetTimerListAdapter(clickListener, viewLifecycleOwner)
        binding.presetTimerList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = presetTimerListAdapter
        }
    }

    private fun subscribeToPresetTimerListObservers() {
        viewModel.presetTimerList.observe(viewLifecycleOwner, Observer {
            presetTimerListAdapter.presetTimers = it
        })

        viewModel.navigateToSettingTimer.observe(viewLifecycleOwner, Observer { it ->
            it?.let {
                val timerName = it["timerName"]
                val presetName = it["presetName"]
                if (timerName != null && presetName != null) {
                    viewModel.getCurrentPresetTimer(timerName, presetName)
                    this.findNavController().navigate(
                        PresetTimerListFragmentDirections.actionPresetTimerListFragmentToSetTimerFragment(
                            timerName = timerName, presetName = presetName
                        )
                    )
                }
                viewModel.doneNavigateToSettingTimer()
            }
        })
    }

    private fun addPresetTimer() {
        viewModel.getNumberOfPresetTimers(args.name)
        if (viewModel.currentNumberOfPresetTimers.value ?: 0 >= Constants.PRESET_TIMER_NUM) {
            Snackbar.make(
                binding.root, "登録できるタイマーは${Constants.PRESET_TIMER_NUM}までです。",
                Snackbar.LENGTH_LONG
            ).show()
        } else {
            this.findNavController().navigate(
                PresetTimerListFragmentDirections.actionPresetTimerListFragmentToSetTimerFragment(
                    timerName = args.name, presetName = null
                )
            )
        }
    }

    private fun deletePresetTimer() {
        viewModel.getNumberOfPresetTimers(args.name)
        if (viewModel.currentNumberOfPresetTimers.value ?: 0 == 0) {
            Snackbar.make(binding.root, R.string.caution, Snackbar.LENGTH_LONG).show()
        } else {
                // TODO Delete処理を記述
        }
    }

    private fun startTimer() {
        this.findNavController().navigate(
            PresetTimerListFragmentDirections.actionPresetTimerListFragmentToTimerFragment(args.name)
        )
    }
}

