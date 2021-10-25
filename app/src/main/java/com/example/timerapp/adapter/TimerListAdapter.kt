package com.example.timerapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.timerapp.database.ListType
import com.example.timerapp.database.Timer
import com.example.timerapp.databinding.ListItemBinding
import com.example.timerapp.databinding.SimpleListItemBinding


class TimerListAdapter :
    ListAdapter<Timer, TimerListAdapter.TimerViewHolder>(TimerDiffCallback()) {

    class DetailViewHolder private constructor(val binding: ListItemBinding) :
        TimerViewHolder(binding) {

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

        override fun bind(timer: Timer) {
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
            fun from(parent: ViewGroup): DetailViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListItemBinding.inflate(layoutInflater, parent, false)
                return DetailViewHolder(binding)
            }
        }
    }

    class SimpleViewHolder private constructor(val binding: SimpleListItemBinding):
        TimerViewHolder(binding){

        override fun bind(timer: Timer) {
            binding.timer = timer
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): SimpleViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = SimpleListItemBinding.inflate(layoutInflater, parent, false)
                return SimpleViewHolder(binding)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimerViewHolder {
        val type = ListType.values()[viewType]
        return when(type) {
            ListType.DETAIL_LAYOUT -> DetailViewHolder.from(parent)
            ListType.SIMPLE_LAYOUT -> SimpleViewHolder.from(parent)
        }
    }

    override fun onBindViewHolder(holder: TimerViewHolder, position: Int) {
        val timer = getItem(position)
        holder.bind(timer)
    }

    // ordinalはenumで定義したViewtypeのIntを返す
    override fun getItemViewType(position: Int): Int {
        return getItem(position).listType.ordinal
    }

    abstract class TimerViewHolder(binding: ViewDataBinding) :
        RecyclerView.ViewHolder(binding.root){
        abstract fun bind(timer: Timer)
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

