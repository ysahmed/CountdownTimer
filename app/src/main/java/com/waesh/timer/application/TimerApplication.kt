package com.waesh.timer.application

import android.app.Application
import com.waesh.timer.service.NotificationModule

class TimerApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        NotificationModule.createNotificationChannel(applicationContext)
    }
}