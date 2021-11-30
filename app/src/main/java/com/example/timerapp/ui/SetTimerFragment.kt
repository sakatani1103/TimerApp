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
import androidx.navigation.fragment.navArgs
import com.example.timerapp.R
import com.example.timerapp.databinding.DialogNotificationTimeBinding
import com.example.timerapp.databinding.FragmentSetTimerBinding
import com.example.timerapp.others.Status
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SetTimerFragment : Fragment() {
    private val args: SetTimerFragmentArgs by navArgs()

    private lateinit var viewModel: TimerViewModel

    private var _binding: FragmentSetTimerBinding? = null
    private val binding: FragmentSetTimerBinding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_set_timer, container, false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[TimerViewModel::class.java]
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        setPresetTimerNameToEt()
        subscribeToSetPresetTimerObservers()

        binding.backBtn.setOnClickListener { this.findNavController().popBackStack() }
        binding.saveButton.setOnClickListener { savePresetTimer() }
        binding.preNotification.setOnClickListener { createNotificationTimeDialog() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setPresetTimerNameToEt() {
        // PresetTimerの+を押して遷移した場合はpresetTimer数字が表示され、
        // PresetTimerを押して遷移した場合はpresetTimer名が表示される
        if (args.presetName == null) {
            val presetTimerList = viewModel.presetTimerList.value ?: listOf()
            val num = if (presetTimerList.isEmpty()) {
                0
            } else {
                presetTimerList.last().timerOrder
            }
            val newName = "presetTimer" + "${num + 1}"
            binding.etPresetName.setText(newName)
        } else {
            binding.etPresetName.setText(args.presetName)
        }
    }

    private fun savePresetTimer() {
        val presetTime = getMilliSeconds()
        val presetName = binding.etPresetName.text.toString()

        if (args.presetName == null) {
            // 新規登録の場合
            viewModel.insertPresetTimer(presetName, presetTime)
        } else {
            // update
            viewModel.updatePresetTimer(presetName, presetTime)
        }
    }

    private fun getMilliSeconds(): Long {
        val num1 = binding.numberPicker1.value.toLong()
        val num2 = binding.numberPicker2.value.toLong()
        val num3 = binding.numberPicker3.value.toLong()
        val num4 = binding.numberPicker4.value.toLong()
        val num5 = binding.numberPicker5.value.toLong()

        val min = num1 * 100L + num2 * 10L + num3
        val sec = num4 * 10L + num5

        val allTime = (min * 60L + sec) * 1000L
        viewModel.getTemporalPresetTime(allTime)
        return allTime
    }

    private fun createNotificationTimeDialog() {
        getMilliSeconds()
        val inflater = requireActivity().layoutInflater
        val binding = DataBindingUtil.inflate<DialogNotificationTimeBinding>(
            inflater,
            R.layout.dialog_notification_time,
            null,
            false
        )
        val createSettingView = binding.root
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        MaterialAlertDialogBuilder(requireContext())
            .setView(createSettingView)
            .setPositiveButton(R.string.save) { _, _ ->
                val min = binding.numberPickerMin.value.toLong()
                val sec = binding.numberPickerSec.value.toLong()
                val allTime = (min * 60L + sec) * 1000L
                viewModel.getTemporalNotificationTime(allTime)
            }
            .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun subscribeToSetPresetTimerObservers() {
        viewModel.insertAndUpdatePresetTimerStatus.observe(viewLifecycleOwner, Observer { it ->
            it.getContentIfNotHandled()?.let { result ->
                if (result.status == Status.SUCCESS) {
                    this.findNavController().popBackStack()
                }
            }
        })
        viewModel.nameStatus.observe(viewLifecycleOwner, Observer { it ->
            it.getContentIfNotHandled()?.let { result ->
                if (result.status == Status.ERROR) {
                    binding.etPresetName.setText(result.data)
                    binding.etTimerName.error = result.message
                }
            }
        })
        viewModel.showTimerError.observe(viewLifecycleOwner, Observer { msg ->
            msg?.let {
                Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG).show()
                viewModel.doneShowTimerError()
            }
        })
    }
}