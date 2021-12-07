package com.example.timerapp.ui.timerlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.timerapp.R
import com.example.timerapp.adapter.TimerListAdapter
import com.example.timerapp.adapter.TimerListListener
import com.example.timerapp.database.ListType
import com.example.timerapp.database.Timer
import com.example.timerapp.databinding.DialogCreateTimerBinding
import com.example.timerapp.databinding.FragmentTimerListBinding
import com.example.timerapp.others.EventObserver
import com.example.timerapp.others.Status
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

class TimerListFragment : Fragment() {
    private var _binding: FragmentTimerListBinding? = null
    private val binding: FragmentTimerListBinding
        get() = _binding!!

    private val viewModel by viewModels<TimerListViewModel>()
    private lateinit var timerListAdapter: TimerListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_timer_list, container, false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        subscribeToTimerListObservers()
        setupRecyclerView()
        viewModel.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupRecyclerView() {
        timerListAdapter =
            TimerListAdapter(
                clickListener = TimerListListener(
                    { timerName -> viewModel.navigateToPresetTimer(timerName) },
                    { timer -> viewModel.navigateToTimer(timer) }),
                viewLifecycleOwner
            )

        binding.timerList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = timerListAdapter
            ItemTouchHelper(itemTouchCallback).attachToRecyclerView(this)
        }
    }

    private val itemTouchCallback = object : ItemTouchHelper.SimpleCallback(
        0, ItemTouchHelper.RIGHT
    ) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val pos = viewHolder.layoutPosition
            val item = timerListAdapter.timerItems[pos]
            viewModel.deleteTimer(item)
        }
    }


    private fun subscribeToTimerListObservers() {
        viewModel.timerItems.observe(viewLifecycleOwner, { timerItems ->
            if (timerItems.isEmpty()) {
                timerListAdapter.timerItems = listOf(Timer("initial", 0, ListType.INITIAL_LAYOUT))
            } else {
                timerListAdapter.timerItems = timerItems
            }
        })

        viewModel.navigateToPresetTimer.observe(viewLifecycleOwner, EventObserver { timerName ->
            this.findNavController().navigate(
                TimerListFragmentDirections.actionTimerListFragmentToPresetTimerListFragment(timerName)
            )
        })

        viewModel.navigateToTimer.observe(viewLifecycleOwner, EventObserver { timerName ->
            this.findNavController().navigate(
                TimerListFragmentDirections.actionTimerListFragmentToTimerFragment(timerName)
            )
        })

        viewModel.navigateToDeleteTimer.observe(viewLifecycleOwner, EventObserver { boolean ->
            if (boolean) {
                this.findNavController().navigate(
                    TimerListFragmentDirections.actionTimerListFragmentToDeleteTimerListFragment()
                )
            }
        })

        viewModel.nameStatus.observe(viewLifecycleOwner, EventObserver { result ->
            when (result.status) {
                Status.ERROR -> {
                    createDialog(result.data, result.message)
                }
                Status.SUCCESS -> {
                    result.data?.let { timerName -> viewModel.insertTimer(timerName) }
                }
            }
        })

        viewModel.deleteTimerItemStatus.observe(viewLifecycleOwner, EventObserver { result ->
            if (result.status == Status.SUCCESS) {
                result.data.let { timer ->
                    if (timer != null) {
                        Snackbar.make(requireView(), "${timer.name}を削除しました。", Snackbar.LENGTH_LONG)
                            .setAction("取り消し") { viewModel.restoreTimerAndPresetTimers(timer) }
                            .show()
                    }
                }
            }
        })

        viewModel.showSnackbarMessage.observe(viewLifecycleOwner, EventObserver { message ->
            Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
        })

        viewModel.showDialog.observe(viewLifecycleOwner, EventObserver { boolean ->
            if (boolean) {
                createDialog(null, null)
            }
        })
    }

    private fun createDialog(inputName: String?, msg: String?) {
        val inflater = requireActivity().layoutInflater
        val binding = DataBindingUtil.inflate<DialogCreateTimerBinding>(
            inflater,
            R.layout.dialog_create_timer,
            null,
            false
        )
        val timerNameInputView = binding.root
        val newName = binding.etTimerName
        val errorLayout = binding.layoutTimerName

        if (msg != null) {
            newName.setText(inputName)
            errorLayout.error = msg
        }

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setCancelable(false)
            .setView(timerNameInputView)
            .setPositiveButton(R.string.save) { _, _ -> viewModel.checkInputTimerName(newName.text.toString()) }
            .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
            .create()
        dialog.show()
    }

}


