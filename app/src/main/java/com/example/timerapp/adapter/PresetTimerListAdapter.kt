package com.example.timerapp.adapter


import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.timerapp.database.PresetTimer
import com.example.timerapp.database.Timer
import com.example.timerapp.databinding.PresetTimerListItemBinding
import com.example.timerapp.databinding.TopListItemBinding
import java.lang.ClassCastException
import kotlin.concurrent.timer

private val ITEM_VIEW_TYPE_HEADER = 0
private val ITEM_VIEW_TYPE_ITEM = 1

class PresetTimerListAdapter(
    private val viewLifeCycleOwner: LifecycleOwner) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var currentTimer: Timer

    private val diffCallback = object : DiffUtil.ItemCallback<PresetTimer>() {
        override fun areItemsTheSame(oldItem: PresetTimer, newItem: PresetTimer): Boolean {
            return oldItem.presetTimerId == newItem.presetTimerId
        }

        override fun areContentsTheSame(oldItem: PresetTimer, newItem: PresetTimer): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    private val differ = AsyncListDiffer(this, diffCallback)

    var presetTimerItems: List<PresetTimer>
        get() = differ.currentList
        set(value) = differ.submitList(value)

    private fun addHeaderToSubmitList(list: List<PresetTimer>?): List<DataItem> {
        return when (list) {
            null -> listOf(DataItem.Header(currentTimer))
            else -> listOf(DataItem.Header(currentTimer)) + list.map { DataItem.PresetTimerItem(it) }
        }
    }

    class HeaderViewHolder private constructor(val binding: TopListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(currentTimer: Timer, viewLifeCycleOwner: LifecycleOwner){
            Log.d("Adapter", currentTimer.toString())
            binding.timer = currentTimer
            binding.lifecycleOwner = viewLifeCycleOwner
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): HeaderViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = TopListItemBinding.inflate(layoutInflater, parent, false)
                return HeaderViewHolder(binding)
            }
        }
    }

    class ViewHolder private constructor(val binding: PresetTimerListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(presetTimer: DataItem.PresetTimerItem, viewLifeCycleOwner: LifecycleOwner) {
            binding.presetTimerItem = presetTimer
            binding.lifecycleOwner = viewLifeCycleOwner
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = PresetTimerListItemBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_VIEW_TYPE_HEADER -> HeaderViewHolder.from(parent)
            ITEM_VIEW_TYPE_ITEM -> ViewHolder.from(parent)
            else -> throw ClassCastException("Unknown viewType ${viewType}")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val items = addHeaderToSubmitList(presetTimerItems)
        when (holder) {
            is ViewHolder -> {
                val item = items[position] as DataItem.PresetTimerItem
                holder.bind(presetTimer = item, viewLifeCycleOwner)
            }
            is HeaderViewHolder -> {
                holder.bind(currentTimer, viewLifeCycleOwner)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            ITEM_VIEW_TYPE_HEADER
        } else {
            ITEM_VIEW_TYPE_ITEM
        }
    }

    override fun getItemCount(): Int {
        return presetTimerItems.size + 1
    }

    fun setCurrentTimer(timer: Timer){
        currentTimer = timer
    }
}

// sealedを使用するとDataclassをobjectを一緒に管理することができる
sealed class DataItem {
    data class PresetTimerItem(val presetTimer: PresetTimer) : DataItem() {
        override val id = presetTimer.presetTimerId
    }

    data class Header(val currentTimer: Timer) : DataItem() {
        override val id = Long.MIN_VALUE
    }

    abstract val id: Long?
}

