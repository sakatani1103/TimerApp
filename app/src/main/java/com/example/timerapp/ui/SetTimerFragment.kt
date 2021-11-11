package com.example.timerapp.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.timerapp.R
import com.example.timerapp.databinding.FragmentSetTimerBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SetTimerFragment : Fragment() {
    private lateinit var viewModel: TimerViewModel

    private var _binding: FragmentSetTimerBinding? = null
    private val binding: FragmentSetTimerBinding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_set_timer, container, false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[TimerViewModel::class.java]
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding= null
    }

}