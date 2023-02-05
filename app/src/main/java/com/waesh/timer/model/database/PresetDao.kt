package com.waesh.timer.model.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.waesh.timer.model.entity.TimerPreset
import kotlinx.coroutines.flow.Flow

@Dao
interface PresetDao {
    @Insert
    suspend fun insertPreset(preset: TimerPreset)

    @Delete
    suspend fun deletePresets(presetList: List<TimerPreset>)

    @Query("SELECT * FROM preset")
    fun getAllPresets(): Flow<List<TimerPreset>>
}