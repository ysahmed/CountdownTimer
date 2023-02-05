package com.waesh.timer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import com.waesh.timer.model.PresetRepository

class HomeViewModel(repository: PresetRepository): ViewModel() {

    val timerPresets = repository.timerPresets.asLiveData()

}

class HomeViewModelFactory(private val repository: PresetRepository): ViewModelProvider.Factory{

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}