package com.example.timerapp.ui.deleteTimerList

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.timerapp.R
import com.example.timerapp.TimerApplication
import com.example.timerapp.adapter.DeleteTimerListAdapter
import com.example.timerapp.adapter.DeleteTimerListListener
import com.example.timerapp.databinding.FragmentDeleteTimerListBinding
import com.example.timerapp.others.EventObserver
import com.example.timerapp.others.Status

class DeleteTimerListFragment : Fragment() {
    private var _binding: FragmentDeleteTimerListBinding? = null
    private val binding: FragmentDeleteTimerListBinding
        get() = _binding!!

    private lateinit var deleteTimerListAdapter: DeleteTimerListAdapter
    private val viewModel by viewModels<DeleteTimerListViewModel>{
        DeleteTimerListViewModelFactory(
            (requireContext().applicationContext as TimerApplication).timerRepository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_delete_timer_list, container, false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        setupRecyclerView()
        subscribeToDeleteTimerListObservers()
        viewModel.start()
    }

    override fun onResume() {
        super.onResume()
        val callback = requireActivity().onBackPressedDispatcher.addCallback(this){
            viewModel.cancelDeleteTimerList()
            isEnabled = false
        }
        callback.isEnabled = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupRecyclerView() {
        deleteTimerListAdapter = DeleteTimerListAdapter(
            clickListener =
            DeleteTimerListListener { timer -> viewModel.switchTimerIsSelected(timer) },
            viewLifecycleOwner
        )
        binding.deleteTimerList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = deleteTimerListAdapter
        }
    }

    private fun subscribeToDeleteTimerListObservers() {
        viewModel.timerItems.observe(viewLifecycleOwner, {
            deleteTimerListAdapter.timerItems = it
        })

        viewModel.deleteTimerItemStatus.observe(viewLifecycleOwner, EventObserver { result ->
            if (result.status == Status.SUCCESS) {
                this.findNavController().popBackStack()
            }
        })
    }

}