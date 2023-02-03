package com.waesh.timer.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.waesh.timer.R
import com.waesh.timer.view.activity.MainActivity

class NotificationModule(private val context: Context) {

    private lateinit var activityPendingIntent: PendingIntent
    private lateinit var broadcastActionPause: PendingIntent
    private lateinit var broadcastActionResume: PendingIntent
    private lateinit var broadcastActionStop: PendingIntent
    private lateinit var broadcastActionDismiss: PendingIntent


    fun getNotificationBase(running: Boolean?, complete: Boolean): NotificationCompat.Builder {
        if (!this@NotificationModule::activityPendingIntent.isInitialized) {
            activityPendingIntent = PendingIntent.getActivity(
                context,
                1,
                Intent(
                    context,
                    MainActivity::class.java
                ).apply {
                    action = MainActivity.ACTION_TIMER_FRAGMENT
                },
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        return NotificationCompat.Builder(
            context,
            COUNTDOWN_NOTIFICATION_CHANNEL_ID
        )
            .setContentTitle("Timer")
            .setSmallIcon(R.drawable.small_icon_timer)
            .setContentIntent(activityPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .apply {

                running?.let {
                    if (running && !complete) {
                        if (!this@NotificationModule::broadcastActionPause.isInitialized) {
                            broadcastActionPause =
                                PendingIntent.getBroadcast(
                                    context,
                                    4443,
                                    Intent(TimerService.ACTION_NOTIFICATION_BUTTON).apply {
                                        putExtra(
                                            TimerService.BROADCAST_INTENT_STRING_EXTRA,
                                            TimerService.ACTION_TIMER_PAUSE
                                        )
                                    },
                                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                                )
                        }
                        addAction(0, "Pause", broadcastActionPause)
                    }

                    if (!running && !complete) {
                        if (!this@NotificationModule::broadcastActionResume.isInitialized) {
                            broadcastActionResume =
                                PendingIntent.getBroadcast(
                                    context,
                                    5554,
                                    Intent(TimerService.ACTION_NOTIFICATION_BUTTON).apply {
                                        putExtra(
                                            TimerService.BROADCAST_INTENT_STRING_EXTRA,
                                            TimerService.ACTION_TIMER_RESUME
                                        )
                                    },
                                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                                )
                        }
                        addAction(0, "Resume", broadcastActionResume)
                    }
                }

                if (!complete) {
                    if (!this@NotificationModule::broadcastActionStop.isInitialized) {
                        broadcastActionStop =
                            PendingIntent.getBroadcast(
                                context,
                                6665,
                                Intent(TimerService.ACTION_NOTIFICATION_BUTTON).apply {
                                    putExtra(
                                        TimerService.BROADCAST_INTENT_STRING_EXTRA,
                                        TimerService.ACTION_TIMER_STOP
                                    )
                                },
                                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                            )
                    }
                    addAction(0, "Stop", broadcastActionStop)
                }

                if (complete) {
                    if (!this@NotificationModule::broadcastActionDismiss.isInitialized) {
                        broadcastActionDismiss =
                            PendingIntent.getBroadcast(
                                context,
                                7776,
                                Intent(TimerService.ACTION_NOTIFICATION_BUTTON).apply {
                                    putExtra(
                                        TimerService.BROADCAST_INTENT_STRING_EXTRA,
                                        TimerService.ACTION_SERVICE_DISMISS
                                    )
                                },
                                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                            )
                    }
                    addAction(0, "Dismiss", broadcastActionDismiss)
                }
            }
    }

    companion object {
        const val COUNTDOWN_NOTIFICATION_CHANNEL_ID = "com.waesh.timer.COUNTDOWN_NOTIFICATION_CHANNEL_ID"
        const val ALARM_NOTIFICATION_CHANNEL_ID = "com.waesh.timer.ALARM_NOTIFICATION_CHANNEL_ID"

        fun createNotificationChannel(context: Context) {
            val countdownNotificationChannel = NotificationChannel(
                COUNTDOWN_NOTIFICATION_CHANNEL_ID,
                "Timer",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                //Like saying DON'T SET A RINGTONE
                setSound(null, null)
                description = "Shows the countdown timer"
            }

            val alarmNotificationChannel = NotificationChannel(
                ALARM_NOTIFICATION_CHANNEL_ID,
                "Counter",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                //Like saying DON'T SET A RINGTONE
                setSound(null, null)
                description = "Alerts when timer ends"
            }

            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(countdownNotificationChannel)

            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(alarmNotificationChannel)

        }
    }
}