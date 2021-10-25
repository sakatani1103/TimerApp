package com.example.timerapp.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.timerapp.database.Timer
import com.example.timerapp.databinding.ListItemBinding


class TimerListAdapter :
    ListAdapter<Timer, TimerListAdapter.ViewHolder>(TimerDiffCallback()) {

    class ViewHolder private constructor(val binding: ListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private var currentItem: Timer? = null

        init {
                binding.detailTitle.setOnClickListener {
                    currentItem?.let {
                        val expanded = it.isExpanded
                        it.isExpanded = expanded.not()
                        // 初期で開いておくところを表示
                        binding.topTopic.isSelected = expanded.not()
                        binding.startBtn.isSelected = expanded.not()

                        binding.expandableLayout.toggle()

                        val anim = RotateAnimation(
                            0f, // 回転の開始角度
                            180f, // 回転の終了角度
                            Animation.RELATIVE_TO_SELF,
                            0.5f,
                            Animation.RELATIVE_TO_SELF,
                            0.5f
                        ).apply {
                            duration = 300
                            fillAfter = true
                        }
                        binding.expandArrow.startAnimation(anim)
                    }
                }
            }

        fun bind(timer: Timer) {
            currentItem = timer

            binding.timer = currentItem

            val expandableLayout = binding.expandableLayout
            if (timer.isExpanded) {
                expandableLayout.expand(false)
            } else {
                expandableLayout.collapse(false)
            }
            binding.expandArrow.isSelected = !timer.isExpanded.not()

            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListItemBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val timer = getItem(position)
        holder.bind(timer)
    }
}

class TimerDiffCallback : DiffUtil.ItemCallback<Timer>() {
    override fun areItemsTheSame(oldItem: Timer, newItem: Timer): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Timer, newItem: Timer): Boolean {
        return oldItem == newItem
    }

}
