package com.example.timerapp.ui.deletePresetTimerList

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.timerapp.R
import com.example.timerapp.adapter.DeletePresetTimerListAdapter
import com.example.timerapp.databinding.FragmentDeletePresetTimerListBinding
import com.example.timerapp.others.EventObserver
import com.example.timerapp.others.Status


class DeletePresetTimerListFragment : Fragment() {
    private var _binding: FragmentDeletePresetTimerListBinding? = null
    private val binding: FragmentDeletePresetTimerListBinding
        get() = _binding!!

    private val args: DeletePresetTimerListFragmentArgs by navArgs()

    private lateinit var deletePresetTimerListAdapter: DeletePresetTimerListAdapter
    private val viewModel by viewModels<DeletePresetTimerListViewModel>()

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
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        viewModel.start(args.timerName)
        setupRecyclerView()
        subscribeToDeletePresetTimerListObservers()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupRecyclerView() {
        deletePresetTimerListAdapter = DeletePresetTimerListAdapter(
            clickListener =
            DeletePresetTimerListAdapter.DeletePresetTimerListListener { presetTimer ->
                viewModel.switchPresetTimerIsSelected(presetTimer)
            }, viewLifecycleOwner
        )
        binding.deletePresetTimerList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = deletePresetTimerListAdapter
        }
    }

    private fun subscribeToDeletePresetTimerListObservers() {
        viewModel.currentPresetTimerList.observe(viewLifecycleOwner, {
            deletePresetTimerListAdapter.presetTimers = it
        })
        viewModel.deletePresetTimerItemStatus.observe(viewLifecycleOwner, EventObserver { result ->
            if (result.status == Status.SUCCESS) {
                viewModel.navigateToPresetTimerList()
            }
        })
        viewModel.navigateToPresetTimerList.observe(viewLifecycleOwner, EventObserver{ timerName ->
            this.findNavController().navigate(DeletePresetTimerListFragmentDirections.actionDeletePresetTimerListFragmentToPresetTimerListFragment(timerName))
        })
    }
}