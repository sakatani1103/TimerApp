package com.example.timerapp.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.example.timerapp.R
import com.example.timerapp.databinding.FragmentTimerListBinding


class PresetTimerListFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DataBindingUtil.inflate<FragmentTimerListBinding>(
            inflater, R.layout.fragment_preset_timer_list, container, false)
        return binding.root
    }

}