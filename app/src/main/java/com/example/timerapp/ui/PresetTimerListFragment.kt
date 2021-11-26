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
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.timerapp.R
import com.example.timerapp.adapter.PresetTimerClickListener
import com.example.timerapp.adapter.PresetTimerListAdapter
import com.example.timerapp.database.NotificationType
import com.example.timerapp.database.Timer
import com.example.timerapp.databinding.FragmentPresetTimerListBinding
import com.example.timerapp.others.Constants
import com.example.timerapp.others.Status
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.lang.IllegalArgumentException

@AndroidEntryPoint
class PresetTimerListFragment : Fragment() {
    private val args: PresetTimerListFragmentArgs by navArgs()
    private lateinit var viewModel: TimerViewModel

    private lateinit var presetTimerListAdapter: PresetTimerListAdapter

    private var _binding: FragmentPresetTimerListBinding? = null
    private val binding: FragmentPresetTimerListBinding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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


        binding.backBtn.setOnClickListener {
            this.findNavController().popBackStack()
        }
        binding.startBtn.setOnClickListener {
            startTimer()
        } // navigateToTimerに変更

        binding.saveUpdate.setOnClickListener {
            updateTimer()
        }
        binding.addList.setOnClickListener {
            viewModel.navigateToSettingTimer(null, null, null)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupRecyclerView() {
        val clickListener = PresetTimerClickListener { timerName, presetName, order ->
            viewModel.navigateToSettingTimer(timerName, presetName, order)
        }
        presetTimerListAdapter =
            PresetTimerListAdapter(clickListener, viewLifecycleOwner)
        binding.presetTimerList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = presetTimerListAdapter
            ItemTouchHelper(itemTouchCallback).attachToRecyclerView(this)
        }
    }

    private val itemTouchCallback = object : ItemTouchHelper.SimpleCallback(
        ItemTouchHelper.UP or ItemTouchHelper.DOWN, ItemTouchHelper.RIGHT
    ){
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            val fromPos = viewHolder.adapterPosition
            val toPos = target.adapterPosition
            viewModel.changePresetTimerOrder(fromPos, toPos)
            presetTimerListAdapter.notifyItemMoved(fromPos, toPos)
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val pos = viewHolder.layoutPosition
            val item = presetTimerListAdapter.presetTimers[pos]
            viewModel.deletePresetTimerAndUpdateTimer(item)
        }
    }

    private fun subscribeToPresetTimerListObservers() {
        viewModel.presetTimerList.observe(viewLifecycleOwner, Observer {
            presetTimerListAdapter.presetTimers = it
        })

        viewModel.navigateToSettingTimer.observe(viewLifecycleOwner, Observer { it ->
            it?.let {
                val timerName = it["timerName"].toString()
                val presetName = it["presetName"].toString()
                val order = it["order"].toString()

                if (presetName != "no name") {
                    // 更新
                    this.findNavController().navigate(
                        PresetTimerListFragmentDirections.actionPresetTimerListFragmentToSetTimerFragment(
                            timerName = timerName, presetName = presetName, order = order
                        )
                    )
                } else {
                    // 新規
                    this.findNavController().navigate(
                        PresetTimerListFragmentDirections.actionPresetTimerListFragmentToSetTimerFragment(
                            timerName = timerName, presetName = null, order = null
                        )
                    )
                }
                viewModel.doneNavigateToSettingTimer()
            }
        })

        viewModel.navigateToDeletePresetTimer.observe(viewLifecycleOwner, Observer {
            it?.let { b ->
                if (b) { this.findNavController().navigate(
                        PresetTimerListFragmentDirections.actionPresetTimerListFragmentToDeletePresetTimerListFragment(args.name))
                        viewModel.doneNavigateToDeletePresetTimer()
                }
            }
        })

        viewModel.deletePresetTimerStatus.observe(viewLifecycleOwner, Observer { it ->
            it.getContentIfNotHandled()?.let { result ->
                if (result.status == Status.SUCCESS) {
                    Snackbar.make(requireView(), "${result.data!!.presetName}を削除しました。",Snackbar.LENGTH_LONG)
                        .setAction("取り消し"){ viewModel.restorePresetTimerAndUpdateTimer(result.data) }
                        .show()
                    }
                }
        })

        viewModel.nameStatus.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { result ->
                when (result.status) {
                    Status.ERROR -> {
                        binding.etTimerName.setText(result.data)
                        binding.timerNameLayout.error = result.message
                    }
                    Status.SUCCESS -> {
                        val notificationType = binding.soundsSpinner.selectedItem as String
                        val inputNotificationType =
                            if (notificationType == "アラーム") NotificationType.ALARM
                            else NotificationType.VIBRATION
                        val isDisplay = binding.settingDisplayTitle.isChecked
                        result.data?.let { timerName -> viewModel.updateTimerNameAndSetting(
                            timerName, inputNotificationType, isDisplay
                        ) }
                    }
                }
            }
        })

        viewModel.showTimerError.observe(viewLifecycleOwner, Observer { error ->
            error?.let {
                Snackbar.make(requireView(), it, Snackbar.LENGTH_LONG).show()
                viewModel.doneShowTimerError()
            }
        })
    }

    private fun startTimer() {
        this.findNavController().navigate(
            PresetTimerListFragmentDirections.actionPresetTimerListFragmentToTimerFragment(args.name)
        )
    }

    private fun updateTimer() {
        val currentTimer = viewModel.currentTimer.value!!
        val timerName = binding.etTimerName.text.toString()
        val notificationType = binding.soundsSpinner.selectedItem as String
        val inputNotificationType =
            if (notificationType == "アラーム") NotificationType.ALARM
            else NotificationType.VIBRATION
        val isDisplay = binding.settingDisplayTitle.isChecked

        if (currentTimer.name != timerName) {
            viewModel.checkInputTimerName(timerName)
        }
        else if (currentTimer.notificationType != inputNotificationType || currentTimer.isDisplay != isDisplay) {
            viewModel.updateSettingTimer(inputNotificationType, isDisplay)
        }
    }
}

