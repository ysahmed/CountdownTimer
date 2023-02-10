package com.waesh.timer.model.database

import androidx.room.*
import com.waesh.timer.model.entity.TimerPreset
import kotlinx.coroutines.flow.Flow

@Dao
interface PresetDao {
    @Insert
    suspend fun insertPreset(preset: TimerPreset)

    @Update
    suspend fun update(preset: TimerPreset)

    @Delete
    suspend fun deletePresets(presetList: List<TimerPreset>)

    @Query("SELECT * FROM preset")
    fun getAllPresets(): Flow<List<TimerPreset>>
}