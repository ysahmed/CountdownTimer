package com.waesh.timer.model

data class TimerPreset(
    val name: String,
    val duration: Long
) {
    val id: Int = 0
}