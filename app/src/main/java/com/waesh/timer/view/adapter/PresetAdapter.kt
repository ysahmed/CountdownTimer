package com.waesh.timer.view.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.waesh.timer.databinding.PresetItemBinding
import com.waesh.timer.model.entity.TimerPreset
import com.waesh.timer.util.UtilMethods

class PresetAdapter(private val itemClickListener: AdapterItemClickListener) :
    ListAdapter<TimerPreset, PresetAdapter.PresetViewHolder>(Comparator()) {

    private var _selectionMode = MutableLiveData(false)
    val selectionMode: LiveData<Boolean>
        get() = _selectionMode

    class Comparator : DiffUtil.ItemCallback<TimerPreset>() {

        override fun areItemsTheSame(oldItem: TimerPreset, newItem: TimerPreset): Boolean {
            Log.i("Compare", "areItemsTheSame: ${oldItem.id == newItem.id}")
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: TimerPreset, newItem: TimerPreset): Boolean {
            Log.i("Compare", "areContentsTheSame: ${oldItem == newItem}")
            return oldItem == newItem
        }
    }

    inner class PresetViewHolder(binding: PresetItemBinding) : ViewHolder(binding.root) {
        val item = binding.root
        val tvPresetName = binding.presetTitle
        val tvTime = binding.presetTime
        val checked = binding.ivCheck
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PresetViewHolder {
        val binding = PresetItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PresetViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PresetViewHolder, position: Int) {
        val currentItem = currentList[position]
        holder.tvPresetName.text = currentItem.name
        holder.tvTime.text = UtilMethods.getFormattedDuration(currentItem.duration)
        holder.item.setOnClickListener {

            if (!_selectionMode.value!!)
                itemClickListener.setPickerFromPreset(currentItem.duration)
            else {
                if (holder.checked.visibility == View.INVISIBLE) {
                    holder.checked.visibility = View.VISIBLE
                    itemClickListener.addSelection(currentItem, position)
                }
                else {
                    holder.checked.visibility = View.INVISIBLE
                    itemClickListener.removeSelection(currentItem, position)
                }
            }
        }

        holder.item.setOnLongClickListener {
            _selectionMode.postValue(true)
            holder.checked.visibility = View.VISIBLE
            itemClickListener.addSelection(currentItem, position)
            true
        }
    }

    fun cancelSelectionMode(){
        _selectionMode.postValue(false)
    }

}

interface AdapterItemClickListener {
    fun setPickerFromPreset(duration: Long)

    fun addSelection(preset: TimerPreset, position: Int)
    fun removeSelection(preset: TimerPreset, position: Int)

}