package com.example.timerapp.adapter


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.timerapp.database.PresetTimer
import com.example.timerapp.databinding.PresetTimerListItemBinding

class PresetTimerListAdapter(
    val clickListener: PresetTimerClickListener,
    val viewLifeCycleOwner: LifecycleOwner) :
    RecyclerView.Adapter<PresetTimerListAdapter.ViewHolder>() {

    private val diffCallback = object : DiffUtil.ItemCallback<PresetTimer>() {
        override fun areItemsTheSame(oldItem: PresetTimer, newItem: PresetTimer): Boolean {
            return oldItem.name == newItem.name && oldItem.presetName == newItem.presetName
        }

        override fun areContentsTheSame(oldItem: PresetTimer, newItem: PresetTimer): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    private val differ = AsyncListDiffer(this, diffCallback)

    var presetTimers: List<PresetTimer>
        get() = differ.currentList
        set(value) = differ.submitList(value)

    class ViewHolder constructor(val binding: PresetTimerListItemBinding)
        : RecyclerView.ViewHolder(binding.root) {
        fun bind(presetTimer: PresetTimer, viewLifeCycleOwner: LifecycleOwner, clickListener: PresetTimerClickListener) {
            binding.presetTimer = presetTimer
            binding.clickListener = clickListener
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val presetTimer = presetTimers[position]
        holder.bind(presetTimer, viewLifeCycleOwner, clickListener)
    }

    override fun getItemCount(): Int {
        return presetTimers.size
    }
}


class PresetTimerClickListener(val clickListener: (timerName: String, presetName: String, order: Int) -> Unit) {
    fun onClick(presetTimer: PresetTimer) = clickListener(presetTimer.name, presetTimer.presetName, presetTimer.timerOrder)
}

