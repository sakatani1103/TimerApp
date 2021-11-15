package com.example.timerapp.ui

import android.os.Bundle
import android.util.Log
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
import androidx.recyclerview.widget.ItemTouchHelper.RIGHT
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.timerapp.R
import com.example.timerapp.adapter.TimerListAdapter
import com.example.timerapp.adapter.TimerListListener
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

        binding.addList.setOnClickListener { addTimer() }
        binding.deleteList.setOnClickListener { deleteTimer() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
            viewModel?.deleteTimer(item.name)
            Snackbar.make(requireView(), "${item.name}を削除しました。", Snackbar.LENGTH_LONG)
                .setAction("取り消し") {
                    viewModel?.insertTimerIntoDb(item)
                    val presetTimerList = viewModel?.presetTimerItemStatus?.value?.data
                    if (presetTimerList != null && presetTimerList.count() > 0) {
                        viewModel?.insertPresetTimerList(presetTimerList)
                    }
                }
                .show()
        }
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

    private fun subscribeToTimerListObservers() {
        viewModel?.navigateToPresetTimer?.observe(viewLifecycleOwner, Observer { name ->
            name?.let {
                viewModel?.getCurrentTimer(name)
                viewModel?.getCurrentTimerWithPresetTimerList(name)
                this.findNavController().navigate(
                    TimerListFragmentDirections.actionTimerListFragmentToPresetTimerListFragment(
                        name
                    )
                )
                viewModel?.doneNavigateToPresetTimer()
            }
        })

        viewModel?.navigateToTimer?.observe(viewLifecycleOwner, Observer { name ->
            name?.let {
                viewModel?.getCurrentTimer(name)
                viewModel?.getCurrentTimerWithPresetTimerList(name)
                this.findNavController().navigate(
                    TimerListFragmentDirections.actionTimerListFragmentToTimerFragment(name)
                )
                viewModel?.doneNavigateToTimer()
            }
        })

        viewModel?.timerItems?.observe(viewLifecycleOwner, Observer {
            timerListAdapter.timerItems = it
        })

        viewModel?.timerNameStatus?.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { result ->
                when (result.status) {
                    Status.ERROR -> {
                        createDialog(result.data, result.message)
                    }
                    Status.SUCCESS -> {
                        if (result.data != null) {
                            viewModel?.navigateToPresetTimer(result.data)
                        }
                    }
                }
            }
        })
    }


    private fun createDialog(inputName: String?, msg: String?) {
        val inflater = requireActivity().layoutInflater
        val createTimerView = inflater.inflate(R.layout.dialog_create_timer, null)
        val newName = createTimerView.findViewById<EditText>(R.id.et_timer_name)
        val errorLayout = createTimerView.findViewById<TextInputLayout>(R.id.layout_timer_name)

        if (msg != null) {
            newName.setText(inputName)
            errorLayout.error = msg
        }

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(createTimerView)
            .setPositiveButton(R.string.save) { _, _ -> viewModel?.insertTimer(newName.text.toString()) }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
        dialog.show()
    }

    private fun addTimer() {
        viewModel?.getNumberOfTimers()
        if (viewModel!!.numberOfTimers.value ?: 0 >= Constants.TIMER_NUM) {
            Snackbar.make(
                binding.root, "登録できるタイマーは${Constants.TIMER_NUM}までです。",
                Snackbar.LENGTH_LONG
            )
                .show()
        } else {
            createDialog(null, null)
        }
    }

    private fun deleteTimer() {
        viewModel?.getNumberOfTimers()
        if (viewModel!!.numberOfTimers.value ?: 0 == 0) {
            Snackbar.make(binding.root, R.string.caution , Snackbar.LENGTH_LONG).show()
        } else {
            // Todo Delete処理を記述
        }
    }
}


