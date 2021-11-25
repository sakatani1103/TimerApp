package com.example.timerapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.timerapp.R
import com.example.timerapp.adapter.TimerListAdapter
import com.example.timerapp.adapter.TimerListListener
import com.example.timerapp.databinding.DialogCreateTimerBinding
import com.example.timerapp.databinding.FragmentTimerListBinding
import com.example.timerapp.others.Constants
import com.example.timerapp.others.Status
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TimerListFragment : Fragment() {

    var viewModel: TimerViewModel? = null

    private lateinit var timerListAdapter: TimerListAdapter

    private var _binding: FragmentTimerListBinding? = null
    private val binding: FragmentTimerListBinding
        get() = _binding!!

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
        viewModel = viewModel ?: ViewModelProvider(requireActivity())[TimerViewModel::class.java]
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        subscribeToTimerListObservers()
        setupRecyclerView()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupRecyclerView() {
        timerListAdapter =
            TimerListAdapter(
                clickListener = TimerListListener(
                    { name -> viewModel?.navigateToPresetTimer(name) },
                    { name -> viewModel?.navigateToTimer(name) }),
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
            // ViewModelの設定後、削除機能の追加
            viewModel?.deleteTimer(item)
        }
    }


    private fun subscribeToTimerListObservers() {
        viewModel?.timerItems?.observe(viewLifecycleOwner, Observer {
            timerListAdapter.timerItems = it
        })

        viewModel?.navigateToPresetTimer?.observe(viewLifecycleOwner, Observer { name ->
            name?.let {
                this.findNavController().navigate(
                    TimerListFragmentDirections.actionTimerListFragmentToPresetTimerListFragment(name)
                )
                viewModel?.doneNavigateToPresetTimer()
            }
        })

        viewModel?.navigateToTimer?.observe(viewLifecycleOwner, Observer { name ->
            name?.let {
                this.findNavController().navigate(
                    TimerListFragmentDirections.actionTimerListFragmentToTimerFragment(name)
                )
                viewModel?.doneNavigateToTimer()
            }
        })

        viewModel?.navigateToDeleteTimer?.observe(viewLifecycleOwner, Observer {
            it?.let { b ->
                if (b) { this.findNavController().navigate(
                        TimerListFragmentDirections.actionTimerListFragmentToDeleteTimerListFragment())
                        viewModel?.doneNavigateToDeleteTimer() }
            }
        })

        viewModel?.timerNameStatus?.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { result ->
                when (result.status) {
                    Status.ERROR -> { createDialog(result.data, result.message) }
                    Status.SUCCESS -> {
                        result.data?.let { timerName -> viewModel!!.insertTimer(timerName) }
                    }
                }
            }
        })

        viewModel?.deleteTimerItemStatus?.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { result ->
                if (result.status == Status.SUCCESS) {
                    result.data.let { timer ->
                        if (timer != null) {
                            Snackbar.make(requireView(), "${timer.name}を削除しました。", Snackbar.LENGTH_LONG)
                                .setAction("取り消し") { viewModel?.restoreTimerAndRelatedPresetTimers(timer) }
                                .show()
                        }
                    }
                }
            }
        })

        viewModel?.showTimerError?.observe(viewLifecycleOwner, Observer { error ->
            error?.let {
                Snackbar.make(requireView(), it, Snackbar.LENGTH_LONG).show()
                viewModel?.doneShowTimerError()
            }
        })

        viewModel?.showTimerDialog?.observe(viewLifecycleOwner, Observer {
            it?.let { b ->
            if(b){ createDialog( null, null )}
                viewModel?.doneShowTimerDialog()
            }
        })
    }

    private fun createDialog(inputName: String?, msg: String?) {
        val inflater = requireActivity().layoutInflater
        val binding = DataBindingUtil.inflate<DialogCreateTimerBinding>(inflater, R.layout.dialog_create_timer, null, false)
        val timerNameInputView = binding.root
        val newName = binding.etTimerName
        val errorLayout = binding.layoutTimerName

        if (msg != null) {
            newName.setText(inputName)
            errorLayout.error = msg
        }

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(timerNameInputView)
            .setPositiveButton(R.string.save) { _, _ -> viewModel?.checkInputTimerName(newName.text.toString()) }
            .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
            .create()
        dialog.show()
    }

}


