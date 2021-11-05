package com.example.timerapp.adapter


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.timerapp.database.PresetTimer
import com.example.timerapp.databinding.PresetTimerListItemBinding
import com.example.timerapp.databinding.TopListItemBinding
import java.lang.ClassCastException

private val ITEM_VIEW_TYPE_HEADER = 0
private val ITEM_VIEW_TYPE_ITEM = 1

class PresetTimerListAdapter(private val _testData: MutableList<PresetTimer>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private fun addHeaderToSubmitList(list: List<PresetTimer>?): List<DataItem> {
        return when (list) {
            null -> listOf(DataItem.Header)
            else -> listOf(DataItem.Header) + list.map { DataItem.PresetTimerItem(it) }
        }
    }

    class HeaderViewHolder private constructor(val binding: TopListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

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
        fun bind(presetTimer: DataItem.PresetTimerItem) {
            binding.presetTimerItem = presetTimer
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
        val items = addHeaderToSubmitList(_testData)
        when (holder) {
            is ViewHolder -> {
                val item = items[position] as DataItem.PresetTimerItem
                holder.bind(presetTimer = item)
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
        return _testData.size + 1
    }
}

// sealedを使用するとDataclassをobjectを一緒に管理することができる
sealed class DataItem {
    data class PresetTimerItem(val presetTimer: PresetTimer) : DataItem() {
        override val id = presetTimer.presetTimerId
    }

    object Header : DataItem() {
        override val id = Long.MIN_VALUE
    }

    abstract val id: Long?
}

