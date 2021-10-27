package com.example.timerapp.adapter


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Database
import com.example.timerapp.database.PresetTimer
import com.example.timerapp.databinding.PresetTimerListItemBinding
import com.example.timerapp.databinding.TopListItemBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.ClassCastException

private val ITEM_VIEW_TYPE_HEADER = 0
private val ITEM_VIEW_TYPE_ITEM = 1

class PresetTimerListAdapter :
    ListAdapter<DataItem, RecyclerView.ViewHolder>(PresetTimerDiffCallback()) {

    private val adapterScope = CoroutineScope(Dispatchers.Default)

    fun addHeaderToSubmitList(list: List<PresetTimer>?){
        adapterScope.launch {
            val items = when (list) {
                null -> listOf(DataItem.Header)
                else -> listOf(DataItem.Header) + list.map { DataItem.PresetTimerItem(it) }
            }
            withContext(Dispatchers.Main) {
                submitList(items)
            }
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
        when (holder) {
            is ViewHolder -> {
                val item = getItem(position) as DataItem.PresetTimerItem
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
}

class PresetTimerDiffCallback : DiffUtil.ItemCallback<DataItem>() {
    override fun areItemsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
        return oldItem == newItem
    }
}

// sealedを使用するとDataclassをobjectを一緒に管理することができる
sealed class DataItem {
    data class PresetTimerItem(val presetTimer: PresetTimer) : DataItem() {
        override val id = presetTimer.id
    }

    object Header : DataItem() {
        override val id = Long.MIN_VALUE
    }

    abstract val id: Long
}

