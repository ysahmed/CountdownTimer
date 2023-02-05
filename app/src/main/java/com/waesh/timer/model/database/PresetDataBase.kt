package com.waesh.timer.model.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.waesh.timer.model.entity.TimerPreset


@Database(entities = [TimerPreset::class], version = 1)
abstract class PresetDataBase : RoomDatabase() {

    abstract fun getPresetDao(): PresetDao

    companion object {

        @Volatile
        private var INSTANCE: PresetDataBase? = null

        fun getDatabase(context: Context): PresetDataBase {

            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PresetDataBase::class.java,
                    "preset_database.db"
                )
                    .createFromAsset("database/preset_database.db")
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}