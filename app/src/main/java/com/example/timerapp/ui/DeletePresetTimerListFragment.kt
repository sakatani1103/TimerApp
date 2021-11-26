package com.example.timerapp.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.clearFragmentResultListener
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.timerapp.R
import com.example.timerapp.adapter.DeletePresetTimerListAdapter
import com.example.timerapp.database.PresetTimer
import com.example.timerapp.databinding.FragmentDeletePresetTimerListBinding
import com.example.timerapp.others.Status
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DeletePresetTimerListFragment : Fragment() {
    private lateinit var viewModel: TimerViewModel

    private lateinit var deletePresetTimerListAdapter: DeletePresetTimerListAdapter

    private var _binding: FragmentDeletePresetTimerListBinding? = null
    private val binding: FragmentDeletePresetTimerListBinding
        get() = _binding!!

    private val deletePresetTimerList = mutableListOf<PresetTimer>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_delete_preset_timer_list, container, false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[TimerViewModel::class.java]
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        setupRecyclerView()
        subscribeToDeletePresetTimerListObservers()

        binding.presetCancelBtn.setOnClickListener {
            viewModel.cancelDeletePresetTimerList(deletePresetTimerList)
        }
        binding.presetDeleteBtn.setOnClickListener {
            viewModel.deletePresetTimerList(deletePresetTimerList)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupRecyclerView(){
        deletePresetTimerListAdapter = DeletePresetTimerListAdapter(clickListener =
        DeletePresetTimerListAdapter.DeletePresetTimerListListener { presetTimer ->
            createDeletePresetList(presetTimer)
        }, viewLifecycleOwner)
        binding.deletePresetTimerList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = deletePresetTimerListAdapter
        }
    }

    private fun subscribeToDeletePresetTimerListObservers() {
        viewModel.presetTimerList.observe(viewLifecycleOwner, Observer {
            deletePresetTimerListAdapter.presetTimers = it
        })
        viewModel.deletePresetTimerStatus.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { result ->
                if (result.status == Status.SUCCESS){
                    deletePresetTimerList.clear()
                    this.findNavController().popBackStack()
                }
            }
        })
    }

    private fun createDeletePresetList(presetTimer: PresetTimer){
        viewModel.switchPresetTimerIsSelected(presetTimer)
        if (deletePresetTimerList.contains(presetTimer)) {
            deletePresetTimerList.remove(presetTimer)
        } else {
            deletePresetTimerList.add(presetTimer)
        }
    }
}