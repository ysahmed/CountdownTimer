package com.waesh.timer.application

import android.app.Application
import com.waesh.timer.model.PresetRepository
import com.waesh.timer.model.database.PresetDataBase
import com.waesh.timer.service.NotificationModule

class TimerApplication: Application() {

    private val database by lazy { PresetDataBase.getDatabase(this) }
    val repository by lazy { PresetRepository(database.getPresetDao()) }

    override fun onCreate() {
        super.onCreate()
        NotificationModule.createNotificationChannel(applicationContext)
    }
}