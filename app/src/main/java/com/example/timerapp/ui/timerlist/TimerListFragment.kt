package com.example.timerapp.ui.timerlist

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.timerapp.R
import com.example.timerapp.TimerApplication
import com.example.timerapp.adapter.TimerListAdapter
import com.example.timerapp.adapter.TimerListListener
import com.example.timerapp.databinding.DialogCreateTimerBinding
import com.example.timerapp.databinding.FragmentTimerListBinding
import com.example.timerapp.others.Constants.URI
import com.example.timerapp.others.EventObserver
import com.example.timerapp.others.Status
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

class TimerListFragment : Fragment() {
    private var _binding: FragmentTimerListBinding? = null
    private val binding: FragmentTimerListBinding
        get() = _binding!!

    private val viewModel by viewModels<TimerListViewModel> {
        TimerListViewModelFactory((requireContext().applicationContext as TimerApplication).timerRepository)
    }
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
        binding.information.setOnClickListener { showInfo() }
        viewModel.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showInfo() {
        MaterialAlertDialogBuilder(requireContext())
            .setCancelable(false)
            .setTitle(R.string.rule_and_policy)
            .setMessage(R.string.rule_and_policy_message)
            .setPositiveButton(R.string.ok) { _, _ -> openWebPage() }
            .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun openWebPage() {
        val uri = Uri.parse(URI)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(intent)
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
                timerListAdapter.timerItems = timerItems
        })

        viewModel.navigateToPresetTimer.observe(viewLifecycleOwner, EventObserver { timerName ->
            this.findNavController().navigate(
                TimerListFragmentDirections.actionTimerListFragmentToPresetTimerListFragment(
                    timerName
                )
            )
        })

        viewModel.navigateToTimer.observe(viewLifecycleOwner, EventObserver { result ->
            when (result.status) {
                Status.SUCCESS -> {
                    result.data?.let { timerName ->
                        this.findNavController().navigate(
                            TimerListFragmentDirections.actionTimerListFragmentToTimerFragment(
                                timerName
                            )
                        )
                    }
                }
                Status.ERROR -> {
                    result.message?.let {
                        Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                    }
                }
            }
        })

        viewModel.navigateToDeleteTimer.observe(viewLifecycleOwner, EventObserver { result ->
            when (result.status) {
                Status.SUCCESS -> this.findNavController().navigate(
                        TimerListFragmentDirections.actionTimerListFragmentToDeleteTimerListFragment()
                    )
                Status.ERROR ->  createSnackbar(result.message)
            }
        })

        viewModel.nameStatus.observe(viewLifecycleOwner, EventObserver { result ->
            when (result.status) {
                Status.ERROR -> createDialog(result.data, result.message)
                Status.SUCCESS -> result.data?.let { timerName -> viewModel.insertTimer(timerName) }
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

        viewModel.showDialog.observe(viewLifecycleOwner, EventObserver { result ->
            when (result.status) {
                Status.SUCCESS -> createDialog(null, null)
                Status.ERROR -> createSnackbar(result.message)
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

    private fun createSnackbar(msg: String?){
        msg?.let {
            Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
        }
    }

}


