package com.waesh.timer.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "preset")
data class TimerPreset(
    var name: String,
    var duration: Long,
    var ringTone_name: String = "Default",
    var ringtone_uri: String = "content://settings/system/alarm_alert"
) {
    @PrimaryKey(autoGenerate = true) var id: Int = 0
}