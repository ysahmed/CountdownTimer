package com.waesh.timer.model

import com.waesh.timer.model.database.PresetDao
import com.waesh.timer.model.entity.TimerPreset

class PresetRepository(private val dao: PresetDao) {

    val timerPresets = dao.getAllPresets()
    suspend fun insertPreset(preset: TimerPreset) = dao.insertPreset(preset)
    suspend fun deletePreset(presetList: List<TimerPreset>) = dao.deletePresets(presetList)

}