package com.example.timerapp.ui.setTimer

import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.timerapp.R
import com.example.timerapp.TimerApplication
import com.example.timerapp.databinding.DialogNotificationTimeBinding
import com.example.timerapp.databinding.FragmentSetTimerBinding
import com.example.timerapp.others.EventObserver
import com.example.timerapp.others.Status
import com.example.timerapp.repository.DefaultTimerRepository
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

class SetTimerFragment : Fragment() {
    private var _binding: FragmentSetTimerBinding? = null
    private val binding: FragmentSetTimerBinding
        get() = _binding!!

    private val args: SetTimerFragmentArgs by navArgs()
    private val viewModel by viewModels<SetTimerViewModel> {
        SetTimerViewModelFactory((requireContext().applicationContext as TimerApplication).timerRepository)
    }

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
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        viewModel.start(presetTimerId = args.presetTimerId, timerName = args.timerName)
        subscribeToSetPresetTimerObservers()
        binding.etPresetName.setOnKeyListener { _, i, keyEvent ->
            return@setOnKeyListener setKeyBoard(view, i, keyEvent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun createNotificationTimeDialog() {
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
            .setCancelable(false)
            .setView(createSettingView)
            .setPositiveButton(R.string.save) { _, _ ->
                viewModel.updateCurrentNotification()
            }
            .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun subscribeToSetPresetTimerObservers() {
        viewModel.insertAndUpdatePresetTimerStatus.observe(
            viewLifecycleOwner,
            EventObserver { result ->
                when (result.status) {
                    Status.ERROR -> {
                        val error = result.data?.get("error")
                        val value = result.data?.get("value")
                        if (error == "name") {
                            binding.etPresetName.setText(value)
                            binding.etTimerName.error = result.message
                        } else {
                            result.message?.let {
                                Snackbar.make(
                                    binding.root,
                                    it, Snackbar.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                    Status.SUCCESS -> {
                        result.data?.get("value")?.let {
                            this.findNavController().popBackStack()
                        }
                    }
                }
            })

        viewModel.showDialog.observe(viewLifecycleOwner, EventObserver { boolean ->
            if (boolean) {
                createNotificationTimeDialog()
            }
        })

        viewModel.navigateToPresetTimer.observe(viewLifecycleOwner, EventObserver {
            if (it) {
                this.findNavController().popBackStack()
            }
        })
    }

    private fun setKeyBoard(view: View, i: Int, keyEvent: KeyEvent): Boolean {
        if ((keyEvent.action == KeyEvent.ACTION_UP && i == KeyEvent.KEYCODE_ENTER) ||
            (keyEvent.action == KeyEvent.ACTION_DOWN && i == KeyEvent.KEYCODE_BACK)
        ) {
            val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, InputMethodManager.RESULT_UNCHANGED_SHOWN)
            return true
        }
        return false
    }
}