package com.example.timerapp.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.timerapp.R
import com.example.timerapp.databinding.FragmentTimerBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TimerFragment : Fragment() {
    val args: TimerFragmentArgs by navArgs()

    private lateinit var viewModel: TimerViewModel
    private var _binding: FragmentTimerBinding? = null
    private val binding: FragmentTimerBinding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DataBindingUtil.inflate<FragmentTimerBinding>(
            inflater, R.layout.fragment_timer, container, false
        )

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[TimerViewModel::class.java]
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        binding.cancelBtn.setOnClickListener {
            createDialog()
        }
        binding.stopOrStartBtn.setOnClickListener {
            // To Do 
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun createDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setMessage(R.string.timer_stop_message)
            .setPositiveButton(R.string.ok){ _, _ -> this.findNavController().popBackStack() }
            .setNegativeButton(R.string.cancel){ dialog, _ -> dialog.dismiss() }
            .show()
    }

}