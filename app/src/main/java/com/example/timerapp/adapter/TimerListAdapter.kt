package com.example.timerapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.timerapp.database.ListType
import com.example.timerapp.database.Timer
import com.example.timerapp.databinding.ListItemBinding
import com.example.timerapp.databinding.SimpleListItemBinding


class TimerListAdapter(
    val clickListener: TimerListListener,
    private val viewLifeCycleOwner: LifecycleOwner
) : RecyclerView.Adapter<TimerListAdapter.TimerViewHolder>() {

    private val diffCallback = object : DiffUtil.ItemCallback<Timer>() {
        override fun areItemsTheSame(oldItem: Timer, newItem: Timer): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: Timer, newItem: Timer): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    private val differ = AsyncListDiffer(this, diffCallback)

    var timerItems: List<Timer>
        get() = differ.currentList
        set(value) = differ.submitList(value)

    class DetailViewHolder constructor(
        val binding: ListItemBinding
    ) : TimerViewHolder(binding) {
        private var expanded: Boolean = false

        init {
            binding.detailTitle.setOnClickListener {

                expanded = !expanded
                // expandedしないところ
                //　設定しないとanimationの矢印が一周してしまう
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

        override fun bind(
            timer: Timer,
            clickListener: TimerListListener,
            viewLifeCycleOwner: LifecycleOwner
        ) {
            binding.timer = timer
            binding.clickListener = clickListener
            binding.lifecycleOwner = viewLifeCycleOwner
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

    class SimpleViewHolder private constructor(
        val binding: SimpleListItemBinding
    ) : TimerViewHolder(binding) {

        override fun bind(
            timer: Timer,
            clickListener: TimerListListener,
            viewLifeCycleOwner: LifecycleOwner
        ) {
            binding.timer = timer
            binding.clickListener = clickListener
            binding.lifecycleOwner = viewLifeCycleOwner
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
        return when (ListType.values()[viewType]) {
            ListType.DETAIL_LAYOUT -> DetailViewHolder.from(parent)
            ListType.SIMPLE_LAYOUT -> SimpleViewHolder.from(parent)
        }
    }

    override fun onBindViewHolder(holder: TimerViewHolder, position: Int) {
        val timer = timerItems[position]
        holder.bind(timer, clickListener, viewLifeCycleOwner)
    }

    // ordinalはenumで定義したViewtypeのIntを返す
    override fun getItemViewType(position: Int): Int {
        return timerItems[position].listType.ordinal
    }

    abstract class TimerViewHolder(binding: ViewDataBinding) :
        RecyclerView.ViewHolder(binding.root) {
        abstract fun bind(
            timer: Timer,
            clickListener: TimerListListener,
            viewLifeCycleOwner: LifecycleOwner
        )
    }

    override fun getItemCount(): Int {
        return timerItems.size
    }
}

class TimerListListener(
    val clickListener: (name: String) -> Unit,
    val startTimer: (timer: Timer) -> Unit
) {
    fun onClick(timer: Timer) = clickListener(timer.name)
    fun onStartClick(timer: Timer) = startTimer(timer)
}


