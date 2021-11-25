package com.example.timerapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.timerapp.database.PresetTimer
import com.example.timerapp.databinding.DeletePresetTimerItemBinding


class DeletePresetTimerListAdapter(
    val clickListener: DeletePresetTimerListListener,
    val viewLifecycleOwner: LifecycleOwner) :
    RecyclerView.Adapter<DeletePresetTimerListAdapter.DeletePresetTimerViewHolder>() {

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

    class DeletePresetTimerViewHolder constructor(val binding: DeletePresetTimerItemBinding)
        : RecyclerView.ViewHolder(binding.root){
            fun bind(presetTimer: PresetTimer, viewLifecycleOwner: LifecycleOwner, clickListener: DeletePresetTimerListListener){
                binding.presetTimer = presetTimer
                binding.lifecycleOwner = viewLifecycleOwner
                binding.clickListener = clickListener
                binding.executePendingBindings()
            }

        companion object {
            fun from(parent: ViewGroup): DeletePresetTimerViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = DeletePresetTimerItemBinding.inflate(layoutInflater, parent, false)
                return DeletePresetTimerViewHolder(binding)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeletePresetTimerViewHolder {
        return DeletePresetTimerViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: DeletePresetTimerViewHolder, position: Int) {
        val presetTimer = presetTimers[position]
        holder.bind(presetTimer, viewLifecycleOwner, clickListener)
    }

    override fun getItemCount(): Int {
        return presetTimers.size
    }

    class DeletePresetTimerListListener(val clickListener: (presetTimer: PresetTimer) -> Unit) {
        fun onClick(presetTimer: PresetTimer) = clickListener(presetTimer)
    }

}