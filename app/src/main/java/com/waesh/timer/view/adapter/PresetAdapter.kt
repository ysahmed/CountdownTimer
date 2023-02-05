package com.waesh.timer.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.waesh.timer.databinding.PresetItemBinding
import com.waesh.timer.model.entity.TimerPreset
import com.waesh.timer.util.UtilMethods

class PresetAdapter(private val itemClickListener: AdapterItemClickListener): ListAdapter<TimerPreset, PresetAdapter.PresetViewHolder>(Comparator()) {

    class Comparator: DiffUtil.ItemCallback<TimerPreset>(){

        override fun areItemsTheSame(oldItem: TimerPreset, newItem: TimerPreset): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: TimerPreset, newItem: TimerPreset): Boolean {
            return oldItem == newItem
        }
    }

    inner class PresetViewHolder(binding: PresetItemBinding) : ViewHolder(binding.root){
        val item = binding.root
        val tvPresetName = binding.presetTitle
        val tvTime = binding.presetTime
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PresetViewHolder {
        val binding = PresetItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val holder = PresetViewHolder(binding)
        return holder
    }

    override fun onBindViewHolder(holder: PresetViewHolder, position: Int) {
        val currentItem = currentList[position]
        holder.tvPresetName.text = currentItem.name
        holder.tvTime.text = UtilMethods.getFormattedDuration(currentItem.duration)
        holder.item.setOnClickListener {
            itemClickListener.setPickerFromPreset(currentItem.duration)
        }
    }
}

interface AdapterItemClickListener{
    fun setPickerFromPreset(duration: Long)
}