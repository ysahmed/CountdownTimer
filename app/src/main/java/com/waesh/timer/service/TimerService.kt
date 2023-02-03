package com.waesh.timer.service

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Binder
import android.os.CountDownTimer
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

@Suppress("WakelockTimeout")
class TimerService : Service() {

    private var countDownTimer: CountDownTimer? = null
    private var millisInFuture = 0L
    private lateinit var duration: String
    private var isTimerRunning = false
    val isTimerActive: Boolean get() = isTimerRunning

    private var _isTimerStopped = MutableLiveData<Boolean>()
    val isTimerStopped: LiveData<Boolean> get() = _isTimerStopped

    private var isTimerPaused = MutableLiveData(false)
    val ifTimerIsPaused: LiveData<Boolean> get() = isTimerPaused

    private val timerServiceBinder = TimerServiceBinder()

    private val _formattedTimeInFuture = MutableLiveData<String>()
    val formattedTimeInFuture: LiveData<String>
        get() = _formattedTimeInFuture

    private var notificationActionReceiver: BroadcastReceiver? = null
    private var timerFinishedAlarmReceiver: BroadcastReceiver? = null

    private val notificationModule = NotificationModule(this)
    //private lateinit var partialWakeLock: WakeLock

    private lateinit var alarmPendingIntent: PendingIntent

    private var mediaPlayer: MediaPlayer? = null

    inner class TimerServiceBinder : Binder() {
        fun getTimerService(): TimerService = this@TimerService
    }

    override fun onCreate() {
        super.onCreate()
        notificationActionReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.hasExtra(BROADCAST_INTENT_STRING_EXTRA) == true) {
                    when (intent.getStringExtra(BROADCAST_INTENT_STRING_EXTRA)) {
                        ACTION_TIMER_PAUSE -> pauseTimer()
                        ACTION_TIMER_STOP -> stopTimer()
                        ACTION_TIMER_RESUME -> resumeTimer()
                        ACTION_SERVICE_DISMISS -> disMiss()
                    }
                }
            }
        }

        registerReceiver(
            notificationActionReceiver,
            IntentFilter(ACTION_NOTIFICATION_BUTTON)
        )

        alarmPendingIntent = PendingIntent.getBroadcast(
            this,
            24457891,
            Intent(
                ACTION_ALARM
            ),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )


        timerFinishedAlarmReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                makeNoise()
                notificationModule.getNotificationBase(running = null, complete = true)
                    .setContentText("Timer ended $duration")
                    .setChannelId(NotificationModule.ALARM_NOTIFICATION_CHANNEL_ID)
                    .build()
                    .notify(TIMER_END_NOTIFICATION_ID)
                stopForeground(STOP_FOREGROUND_REMOVE)
            }
        }

        registerReceiver(
            timerFinishedAlarmReceiver,
            IntentFilter(ACTION_ALARM)
        )

/*
        partialWakeLock = (getSystemService(Context.POWER_SERVICE) as PowerManager)
            .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, packageName)*/
    }

    override fun onBind(intent: Intent): IBinder {
        Log.i(TAG, "onBind: Bound!")
        return timerServiceBinder
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (intent.hasExtra(MILLIS_IN_FUTURE)) {
            millisInFuture = intent.getLongExtra(MILLIS_IN_FUTURE, 0L)
            duration = getFormattedDuration()
            _formattedTimeInFuture.postValue(duration)
            Log.i(TAG, "onBind: millisInFuture = $millisInFuture")
        }
        startForeground(
            TIMER_NOTIFICATION_ID,
            notificationModule.getNotificationBase(running = true, complete = false)
                .setContentText(duration)
                .build()
        )

        return START_NOT_STICKY
    }

    fun startTimer() {
        _isTimerStopped.postValue(false)
        isTimerRunning = true

        //Log.i(TAG, "startTimer: called")

        //partialWakeLock.acquire()

        // set alarm
        (getSystemService(ALARM_SERVICE) as AlarmManager).setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + millisInFuture,
            alarmPendingIntent
        )

        countDownTimer = object : CountDownTimer(
            millisInFuture + 250,
            50
        ) {
            var oneSecond = 0L
            override fun onTick(millisUntilFinished: Long) {
                millisInFuture = millisUntilFinished
                oneSecond += 50
                if (oneSecond >= 999) {
                    oneSecond = 0L
                    //Log.i(TAG, "onTick: Tick... $millisInFuture ")
                    _formattedTimeInFuture.postValue(getFormattedDuration())
                    notificationModule.getNotificationBase(running = true, complete = false)
                        .setContentText(_formattedTimeInFuture.value)
                        .build()
                        .notify()
                }
            }

            override fun onFinish() {
                isTimerRunning = false
                _isTimerStopped.postValue(true)
                // stopForeground(STOP_FOREGROUND_REMOVE)
                /*makeNoise()
                notificationModule.getNotificationBase(running = null, complete = true)
                    .setContentText("Timer ended $duration")
                    .setChannelId(NotificationModule.ALARM_NOTIFICATION_CHANNEL_ID)
                    .build()
                    .notify(TIMER_END_NOTIFICATION_ID)*/
                //partialWakeLock.release()
            }
        }.start()
    }

    fun pauseTimer() {
        countDownTimer?.cancel()
        // cancel alarm
        (getSystemService(ALARM_SERVICE) as AlarmManager).cancel(
            alarmPendingIntent
        )
        notificationModule.getNotificationBase(running = false, complete = false)
            .setContentText(_formattedTimeInFuture.value)
            .build()
            .notify()
        isTimerPaused.postValue(true)
        //partialWakeLock.release()
    }

    fun resumeTimer() {
        startTimer()
        isTimerPaused.postValue(false)
    }

    fun stopTimer() {
        _isTimerStopped.postValue(true)
        countDownTimer?.cancel()
        isTimerPaused.postValue(false)
        isTimerRunning = false

        // cancel alarm
        (getSystemService(ALARM_SERVICE) as AlarmManager).cancel(
            alarmPendingIntent
        )

        //partialWakeLock.release()

        disMiss()
    }

    private fun disMiss() {
        //partialWakeLock.release()
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .cancel(
                TIMER_END_NOTIFICATION_ID
            )
        stopForeground(STOP_FOREGROUND_REMOVE)
        mediaPlayer?.apply {
            stop()
            reset()
            release()
        }
        stopSelf()
    }


    private fun getFormattedDuration(): String {
        val hour = (millisInFuture / (1000 * 60 * 60)) % 24
        val minute = (millisInFuture / (1000 * 60)) % 60
        val second = (millisInFuture / 1000) % 60

        return String.format("%02d:%02d:%02d", hour, minute, second)
    }

    private fun makeNoise() {
        val sound: Uri =
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        mediaPlayer = MediaPlayer()

        try {
            mediaPlayer!!.apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build()
                )
                setDataSource(this@TimerService, sound)
                prepare()
                start()
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun Notification.notify(id: Int = TIMER_NOTIFICATION_ID) {
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .notify(id, this)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(notificationActionReceiver)
        unregisterReceiver(timerFinishedAlarmReceiver)
        notificationActionReceiver = null
        timerFinishedAlarmReceiver = null
        countDownTimer = null
        mediaPlayer = null
        Log.i(TAG, "onDestroy: Called")
    }


    companion object {
        const val TAG = "TIMER_SERVICE"
        const val MILLIS_IN_FUTURE = "MILLIS_IN_FUTURE"
        const val ACTION_NOTIFICATION_BUTTON = "com.waesh.timer.NOTIFICATION_ACTION"
        const val ACTION_ALARM = "com.waesh.timer.ACTION_ALARM"
        const val BROADCAST_INTENT_STRING_EXTRA = "BROADCAST_INTENT_STRING_EXTRA"
        const val ACTION_TIMER_PAUSE = "com.waesh.timer.ACTION_PAUSE_TIMER"
        const val ACTION_TIMER_STOP = "com.waesh.timer.ACTION_STOP_TIMER"
        const val ACTION_TIMER_RESUME = "com.waesh.timer.ACTION_RESUME_TIMER"
        const val ACTION_SERVICE_DISMISS = "com.waesh.timer.DISMISS_SERVICE"
        const val TIMER_NOTIFICATION_ID = 224687
        const val TIMER_END_NOTIFICATION_ID = 854764
    }
}