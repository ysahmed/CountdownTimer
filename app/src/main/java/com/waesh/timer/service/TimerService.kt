package com.waesh.timer.service

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Binder
import android.os.CountDownTimer
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.waesh.timer.util.UtilMethods

class TimerService : Service() {

    private var countDownTimer: CountDownTimer? = null

    private val _millis = MutableLiveData<Long>()
    val millis: LiveData<Long>
        get() = _millis

    private var millisInFuture = 0L
        set(value) {
            _millis.postValue(value)
            field = value
        }

    private lateinit var duration: String
    //private var isTimerRunning = false
    //val isTimerActive: Boolean get() = isTimerRunning

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

    private lateinit var alarmPendingIntent: PendingIntent

    //private var mediaPlayer: MediaPlayer? = null
    private lateinit var ringtone: Ringtone

    inner class TimerServiceBinder : Binder() {
        fun getTimerService(): TimerService = this@TimerService
    }

    override fun onCreate() {
        super.onCreate()
        notificationActionReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.hasExtra(BROADCAST_INTENT_STRING_EXTRA) == true) {
                    when (intent.getStringExtra(BROADCAST_INTENT_STRING_EXTRA)) {
                        ACTION_TIMER_START -> startTimer()
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
                //makeNoise()
                ringtone.play()
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
    }

    override fun onBind(intent: Intent): IBinder {
        Log.i(TAG, "onBind: Bound!")
        return timerServiceBinder
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (intent.hasExtra(MILLIS_IN_FUTURE)) {
            millisInFuture = intent.getLongExtra(MILLIS_IN_FUTURE, 0L)
            duration = UtilMethods.getFormattedDuration(millisInFuture)
            _formattedTimeInFuture.postValue(duration)
        }

        if (intent.hasExtra(RINGTONE_URI)) {
            ringtone = RingtoneManager.getRingtone(
                this,
                Uri.parse(
                    intent.getStringExtra(
                        RINGTONE_URI
                    )
                )
            )
        }

        ringtone.audioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .setUsage(AudioAttributes.USAGE_ALARM)
            .build()

        startForeground(
            TIMER_COUNTDOWN_NOTIFICATION_ID,
            notificationModule.getNotificationBase(running = true, complete = false)
                .setContentText(duration)
                .build()
        )

        return START_NOT_STICKY
    }

    private fun startTimer() {
        _isTimerStopped.postValue(false)
        //isTimerRunning = true

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
                    _formattedTimeInFuture.postValue(UtilMethods.getFormattedDuration(millisInFuture))
                    notificationModule.getNotificationBase(running = true, complete = false)
                        .setContentText(_formattedTimeInFuture.value)
                        .build()
                        .notify()
                }
            }

            override fun onFinish() {
                //isTimerRunning = false
                _isTimerStopped.postValue(true)
            }
        }.start()
    }

    private fun pauseTimer() {
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
    }

    private fun resumeTimer() {
        startTimer()
        isTimerPaused.postValue(false)
    }

    private fun stopTimer() {
        _isTimerStopped.postValue(true)
        countDownTimer?.cancel()
        isTimerPaused.postValue(false)
        //isTimerRunning = false

        // cancel alarm
        (getSystemService(ALARM_SERVICE) as AlarmManager).cancel(
            alarmPendingIntent
        )

        disMiss()
    }

    private fun disMiss() {
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .cancel(
                TIMER_END_NOTIFICATION_ID
            )
        stopForeground(STOP_FOREGROUND_REMOVE)
/*        mediaPlayer?.apply {
            stop()
            reset()
            release()
        }*/
        ringtone.stop()
        stopSelf()
    }

/*    private fun makeNoise() {
        val sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

        mediaPlayer = MediaPlayer()
        Log.i(TAG, "makeNoise: $sound")

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
    }*/

    private fun Notification.notify(id: Int = TIMER_COUNTDOWN_NOTIFICATION_ID) {
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
        //mediaPlayer = null
        Log.i(TAG, "onDestroy: Called")
    }

    companion object {
        const val TAG = "TIMER_SERVICE"
        const val MILLIS_IN_FUTURE = "MILLIS_IN_FUTURE"
        const val RINGTONE_URI = "RINGTONE_URI"
        const val ACTION_NOTIFICATION_BUTTON = "com.waesh.timer.NOTIFICATION_ACTION"
        const val ACTION_ALARM = "com.waesh.timer.ACTION_ALARM"
        const val BROADCAST_INTENT_STRING_EXTRA = "BROADCAST_INTENT_STRING_EXTRA"

        const val ACTION_TIMER_START = "com.waesh.timer.ACTION_START_TIMER"
        const val ACTION_TIMER_PAUSE = "com.waesh.timer.ACTION_PAUSE_TIMER"
        const val ACTION_TIMER_STOP = "com.waesh.timer.ACTION_STOP_TIMER"
        const val ACTION_TIMER_RESUME = "com.waesh.timer.ACTION_RESUME_TIMER"

        const val ACTION_SERVICE_DISMISS = "com.waesh.timer.DISMISS_SERVICE"
        const val TIMER_COUNTDOWN_NOTIFICATION_ID = 224687
        const val TIMER_END_NOTIFICATION_ID = 854764
    }
}