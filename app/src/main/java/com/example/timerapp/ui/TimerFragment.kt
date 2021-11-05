package com.example.timerapp.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.example.timerapp.R
import com.example.timerapp.databinding.FragmentTimerBinding


class TimerFragment : Fragment() {

    private var _binding: FragmentTimerBinding? = null
    private val binding: FragmentTimerBinding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DataBindingUtil.inflate<FragmentTimerBinding>(
            inflater,R.layout.fragment_timer,container,false)

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding= null
    }

}