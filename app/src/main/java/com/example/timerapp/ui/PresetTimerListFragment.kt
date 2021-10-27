package com.example.timerapp.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.example.timerapp.R
import com.example.timerapp.adapter.PresetTimerListAdapter
import com.example.timerapp.database.PresetTimer
import com.example.timerapp.databinding.FragmentPresetTimerListBinding
import com.example.timerapp.databinding.FragmentTimerListBinding


class PresetTimerListFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DataBindingUtil.inflate<FragmentPresetTimerListBinding>(
            inflater, R.layout.fragment_preset_timer_list, container, false)

        val list: MutableList<PresetTimer> = mutableListOf()
        list.add(
            PresetTimer(1,"基本情報技術者午前試験", "設問1", "20分",
        "5分前")
        )
        list.add(PresetTimer(2,"基本情報技術者午前試験", "設問2-5", "30分",
            "10分前"))
        list.add(PresetTimer(2,"基本情報技術者午前試験", "設問2-5", "30分",
            "10分前"))
        list.add(PresetTimer(2,"基本情報技術者午前試験", "設問2-5", "30分",
            "10分前"))

        val presetTimerListAdapter = PresetTimerListAdapter()
        binding.presetTimerList.adapter = presetTimerListAdapter
        presetTimerListAdapter.addHeaderToSubmitList(list)

        return binding.root
    }

}