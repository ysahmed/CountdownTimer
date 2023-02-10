package com.waesh.timer.viewmodel

import android.util.Log
import androidx.lifecycle.*
import com.waesh.timer.model.entity.TimerPreset
import com.waesh.timer.model.repository.PresetRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: PresetRepository) : ViewModel() {

    val timerPresets = repository.timerPresets.asLiveData()
    private var _selections: MutableList<TimerPreset> = mutableListOf()
    private val dispatcher = Dispatchers.IO

    fun insertNew(preset: TimerPreset) {
        viewModelScope.launch(dispatcher) {
            repository.insertPreset(preset)
        }
    }

    fun addSelection(preset: TimerPreset) {
        _selections.add(preset)
    }

    fun removeSelection(preset: TimerPreset) {
        _selections.remove(preset)
    }

    fun deleteSelections(): Boolean {
        Log.i("kkkCat", "deleteSelection: ${_selections.size}")
        viewModelScope.launch(dispatcher) {
            repository.deletePresets(_selections.toList())
            _selections = mutableListOf()
        }
        return true
    }

    fun getFirstSelection(): TimerPreset = _selections[0]

    fun update(preset: TimerPreset) {
        viewModelScope.launch(dispatcher) {
            repository.update(preset)
            _selections = mutableListOf()
        }
    }
}

class HomeViewModelFactory(private val repository: PresetRepository) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}