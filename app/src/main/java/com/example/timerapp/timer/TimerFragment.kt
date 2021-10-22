package com.example.timerapp.timer

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.example.timerapp.R
import com.example.timerapp.databinding.FragmentTimerBinding


class TimerFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DataBindingUtil.inflate<FragmentTimerBinding>(
            inflater,R.layout.fragment_timer,container,false)
        return binding.root
    }

}