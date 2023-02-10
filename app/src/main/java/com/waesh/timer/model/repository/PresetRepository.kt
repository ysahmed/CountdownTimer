package com.waesh.timer.model.repository

import com.waesh.timer.model.database.PresetDao
import com.waesh.timer.model.entity.TimerPreset

class PresetRepository(private val dao: PresetDao) {

    val timerPresets = dao.getAllPresets()
    suspend fun insertPreset(preset: TimerPreset) = dao.insertPreset(preset)

    suspend fun update(preset: TimerPreset) = dao.update(preset)

    suspend fun deletePresets(presetList: List<TimerPreset>) {
        dao.deletePresets(presetList)
    }

}