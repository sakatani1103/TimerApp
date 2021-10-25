package com.example.timerapp.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.example.timerapp.R
import com.example.timerapp.adapter.TimerListAdapter
import com.example.timerapp.database.ListType
import com.example.timerapp.database.Timer
import com.example.timerapp.databinding.FragmentTimerListBinding

class TimerListFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DataBindingUtil.inflate<FragmentTimerListBinding>(
            inflater, R.layout.fragment_timer_list, container, false)

        val list: MutableList<Timer> = mutableListOf()
        list.add(Timer(1, "Timer1", "2時間30分", "詳細\n詳細\n詳細\n",
        ListType.SIMPLE_LAYOUT,false, "10分", false))
        list.add(Timer(1, "Timer2", "2時間30分", "詳細\n詳細\n詳細\n",
            ListType.DETAIL_LAYOUT,false, "10分", false))
        list.add(Timer(1, "Timer3", "2時間30分", "詳細\n詳細\n詳細\n",
            ListType.DETAIL_LAYOUT,false, "10分", false))
        list.add(Timer(1, "Timer4", "2時間30分", "詳細\n詳細\n詳細\n",
            ListType.DETAIL_LAYOUT,false, "10分", false))
        list.add(Timer(1, "Timer5", "2時間30分", "詳細\n詳細\n詳細\n",
            ListType.SIMPLE_LAYOUT,false, "10分", false))
        list.add(Timer(1, "Timer1", "2時間30分", "詳細\n詳細\n詳細\n",
            ListType.SIMPLE_LAYOUT,false, "10分", false))
        list.add(Timer(1, "Timer1", "2時間30分", "詳細\n詳細\n詳細\n",
            ListType.SIMPLE_LAYOUT,false, "10分", false))

        val timerListAdapter = TimerListAdapter()
        binding.timerList.adapter = timerListAdapter
        timerListAdapter.submitList(list)

        return binding.root
    }
}