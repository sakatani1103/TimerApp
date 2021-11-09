package com.example.timerapp.ui


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.RIGHT
import androidx.recyclerview.widget.RecyclerView
import com.example.timerapp.R
import com.example.timerapp.adapter.PresetTimerListAdapter
import com.example.timerapp.adapter.TimerListAdapter
import com.example.timerapp.database.ListType
import com.example.timerapp.database.NotificationType
import com.example.timerapp.database.PresetTimer
import com.example.timerapp.database.Timer
import com.example.timerapp.databinding.FragmentTimerBinding
import com.example.timerapp.databinding.FragmentTimerListBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TimerListFragment : Fragment() {
    private lateinit var viewModel: TimerListViewModel

    private var _binding: FragmentTimerListBinding? = null
    private val binding: FragmentTimerListBinding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_timer_list, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[TimerListViewModel::class.java]

        val list = createTestList()
        setupRecyclerView(list)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding= null
    }

    private val itemTouchCallback = object : ItemTouchHelper.SimpleCallback(
        0, RIGHT
    ){
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val pos = viewHolder.layoutPosition
            val list = createTestList()
            val item = list[pos]
            // ViewModelの設定後、削除機能の追加
        }

    }

    private fun setupRecyclerView(data: MutableList<Timer>) {
        binding.timerList.apply {
            adapter = TimerListAdapter(data)
        }
    }

    private fun createTestList() : MutableList<Timer>{
        val list: MutableList<Timer> = mutableListOf()
        list.add(Timer("Timer1", 150, ListType.SIMPLE_LAYOUT,NotificationType.VIBRATION))
        list.add(Timer("Timer2", 150, ListType.DETAIL_LAYOUT,NotificationType.VIBRATION))
        list.add(Timer("Timer3", 20, ListType.SIMPLE_LAYOUT,NotificationType.ALARM))
        list.add(Timer("Timer4", 300, ListType.DETAIL_LAYOUT,NotificationType.ALARM))
        list.add(Timer("Timer1", 150, ListType.SIMPLE_LAYOUT,NotificationType.VIBRATION))
        list.add(Timer("Timer2", 150, ListType.DETAIL_LAYOUT,NotificationType.VIBRATION))
        list.add(Timer("Timer3", 90, ListType.SIMPLE_LAYOUT,NotificationType.ALARM))
        list.add(Timer("Timer4", 150, ListType.DETAIL_LAYOUT,NotificationType.ALARM))
        return list
    }

}