package com.example.timerapp.adapter

import android.text.BoringLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.AdapterView
import androidx.annotation.NonNull
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.timerapp.database.ListType
import com.example.timerapp.database.Timer
import com.example.timerapp.databinding.DeleteListItemBinding
import com.example.timerapp.databinding.DeleteSimpleListItemBinding
import com.example.timerapp.databinding.ListItemBinding
import com.example.timerapp.databinding.SimpleListItemBinding

class DeleteTimerListAdapter(
    val clickListener: DeleteTimerListListener,
    val viewLifecycleOwner: LifecycleOwner)
    : RecyclerView.Adapter<DeleteTimerListAdapter.DeleteTimerViewHolder>() {

    private val diffCallback = object: DiffUtil.ItemCallback<Timer>(){
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

    class DeleteDetailViewHolder(
        val binding: DeleteListItemBinding
    ) : DeleteTimerListAdapter.DeleteTimerViewHolder(binding){
        private var expanded: Boolean = false

        init {
            binding.detailTitle.setOnClickListener {

                expanded = !expanded
                // expandedしないところ
                //　設定しないとanimationの矢印が一周してしまう
                binding.topTopic.isSelected = expanded.not()

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

        override fun bind(timer: Timer, viewLifecycleOwner: LifecycleOwner, clickListener: DeleteTimerListListener) {
            binding.timer = timer
            binding.clickListener = clickListener
            binding.lifecycleOwner = viewLifecycleOwner
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): DeleteDetailViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = DeleteListItemBinding.inflate(layoutInflater, parent, false)
                return DeleteDetailViewHolder(binding)
            }
        }
    }

    class DeleteSimpleViewHolder private constructor(
        val binding: DeleteSimpleListItemBinding
    ) : DeleteTimerListAdapter.DeleteTimerViewHolder(binding){

        override fun bind(timer: Timer, viewLifecycleOwner: LifecycleOwner, clickListener: DeleteTimerListListener) {
            binding.timer = timer
            binding.clickListener = clickListener
            binding.lifecycleOwner = viewLifecycleOwner
            binding.executePendingBindings()
        }
        companion object {
            fun from(parent: ViewGroup): DeleteSimpleViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = DeleteSimpleListItemBinding.inflate(layoutInflater, parent, false)
                return DeleteSimpleViewHolder(binding)
            }
        }
    }

    abstract class DeleteTimerViewHolder constructor(binding: ViewDataBinding) :
            RecyclerView.ViewHolder(binding.root){
                abstract fun bind(timer: Timer, viewLifecycleOwner: LifecycleOwner, clickListener: DeleteTimerListListener)
            }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeleteTimerViewHolder {
        return when(ListType.values()[viewType]){
            ListType.DETAIL_LAYOUT -> DeleteDetailViewHolder.from(parent)
            ListType.SIMPLE_LAYOUT -> DeleteSimpleViewHolder.from(parent)
        }
    }

    override fun onBindViewHolder(holder: DeleteTimerViewHolder, position: Int) {
        val timer = timerItems[position]
        holder.bind(timer, viewLifecycleOwner, clickListener)
    }

    override fun getItemCount(): Int {
        return timerItems.size
    }

    override fun getItemViewType(position: Int): Int {
        return timerItems[position].listType.ordinal
    }
}

class DeleteTimerListListener(val clickListener: (timer: Timer) -> Unit) {
    fun onClick(timer: Timer) = clickListener(timer)
}