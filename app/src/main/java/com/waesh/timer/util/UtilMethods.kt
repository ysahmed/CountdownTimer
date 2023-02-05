package com.waesh.timer.util

object UtilMethods {

    fun getFormattedDuration(millis: Long): String {
        val hour = (millis / (1000 * 3600)) % 24
        val minute = (millis / (1000 * 60)) % 60
        val second = (millis / 1000) % 60

        return String.format("%02d:%02d:%02d", hour, minute, second)
    }
}