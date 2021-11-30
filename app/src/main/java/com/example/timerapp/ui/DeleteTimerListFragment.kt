package com.example.timerapp.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.timerapp.R
import com.example.timerapp.adapter.DeleteTimerListAdapter
import com.example.timerapp.adapter.DeleteTimerListListener
import com.example.timerapp.database.Timer
import com.example.timerapp.databinding.FragmentDeleteTimerListBinding
import com.example.timerapp.others.Status
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class DeleteTimerListFragment : Fragment() {
    private lateinit var viewModel: TimerViewModel

    private lateinit var deleteTimerListAdapter: DeleteTimerListAdapter

    private var _binding: FragmentDeleteTimerListBinding? = null
    private val binding: FragmentDeleteTimerListBinding
        get() = _binding!!


    private val deleteTimerList = mutableListOf<Timer>()

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
        viewModel = ViewModelProvider(requireActivity())[TimerViewModel::class.java]
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        setupRecyclerView()
        subscribeToDeleteTimerListObservers()

        binding.cancelBtn.setOnClickListener {
            viewModel.cancelDeleteTimerList(deleteTimerList)
        }

        binding.deleteBtn.setOnClickListener {
            viewModel.deleteTimerListAndPresetTimerList(deleteTimerList)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupRecyclerView() {
        deleteTimerListAdapter = DeleteTimerListAdapter(clickListener =
        DeleteTimerListListener { timer -> createDeleteList(timer) }, viewLifecycleOwner
        )
        binding.deleteTimerList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = deleteTimerListAdapter
        }
    }

    private fun subscribeToDeleteTimerListObservers() {
        viewModel.timerItems.observe(viewLifecycleOwner, Observer {
            deleteTimerListAdapter.timerItems = it
        })

        viewModel.deleteTimerItemStatus.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { result ->
                if (result.status == Status.SUCCESS) {
                    deleteTimerList.clear()
                    this.findNavController().popBackStack()
                }
            }
        })
    }

    private fun createDeleteList(timer: Timer) {
        viewModel.switchTimerIsSelected(timer)
        if (deleteTimerList.contains(timer)) {
            deleteTimerList.remove(timer)
        } else {
            deleteTimerList.add(timer)
        }
    }

}