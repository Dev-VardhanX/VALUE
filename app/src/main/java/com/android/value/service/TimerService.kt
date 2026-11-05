package com.android.value.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.android.value.MainActivity
import com.android.value.R
import com.android.value.core.AlarmSoundManager
import com.android.value.core.TimeUtils
import com.android.value.core.TimerEngine
import com.android.value.model.TimerStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Foreground service that keeps the timer running reliably in the background,
 * displays an ongoing status bar notification with actions, and fires alarm/vibration on completion.
 */
class TimerService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var observeJob: Job? = null

    private var wakeLock: PowerManager.WakeLock? = null
    private var alarmSoundManager: AlarmSoundManager? = null

    private var lastNotifiedSecond: Long = -1L
    private var lastNotifiedStatus: TimerStatus? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        acquireWakeLock()
        alarmSoundManager = AlarmSoundManager(this)

        startObservingTimerEngine()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val initialNotification = buildNotification("VALUE", "Starting timer...", false)
                startServiceInForeground(initialNotification)
            }
            ACTION_PAUSE -> {
                TimerEngine.pause(this)
            }
            ACTION_RESUME -> {
                TimerEngine.resume(this)
            }
            ACTION_RESET -> {
                TimerEngine.reset(this)
                stopForegroundAndFinish()
            }
            ACTION_DISMISS -> {
                alarmSoundManager?.stopAlarm()
                TimerEngine.dismiss(this)
                stopForegroundAndFinish()
            }
            ACTION_FINISH -> {
                alarmSoundManager?.startAlarm()
                showFinishedNotification()
            }
        }
        return START_NOT_STICKY
    }

    private fun startObservingTimerEngine() {
        observeJob?.cancel()
        observeJob = serviceScope.launch {
            TimerEngine.snapshot.collectLatest { snapshot ->
                when (snapshot.status) {
                    TimerStatus.Running -> {
                        val currentSecond = (snapshot.remainingMillis + 999) / 1000
                        if (currentSecond != lastNotifiedSecond || lastNotifiedStatus != snapshot.status) {
                            lastNotifiedSecond = currentSecond
                            lastNotifiedStatus = snapshot.status
                            val formattedTime = TimeUtils.formatHms(snapshot.remainingMillis)
                            val notification = buildOngoingNotification(formattedTime, isPaused = false)
                            updateNotification(notification)
                        }
                    }
                    TimerStatus.Paused -> {
                        if (lastNotifiedStatus != snapshot.status) {
                            lastNotifiedStatus = snapshot.status
                            val formattedTime = TimeUtils.formatHms(snapshot.remainingMillis)
                            val notification = buildOngoingNotification(formattedTime, isPaused = true)
                            updateNotification(notification)
                        }
                    }
                    TimerStatus.Finished -> {
                        if (lastNotifiedStatus != snapshot.status) {
                            lastNotifiedStatus = snapshot.status
                            alarmSoundManager?.startAlarm()
                            showFinishedNotification()
                        }
                    }
                    TimerStatus.Idle -> {
                        if (lastNotifiedStatus != null && lastNotifiedStatus != TimerStatus.Idle) {
                            stopForegroundAndFinish()
                        }
                    }
                }
            }
        }
    }

    private fun startServiceInForeground(notification: Notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun updateNotification(notification: Notification) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun buildOngoingNotification(formattedTime: String, isPaused: Boolean): Notification {
        val openAppPendingIntent = createOpenAppPendingIntent()

        val pauseResumeAction = if (isPaused) {
            val resumeIntent = Intent(this, TimerService::class.java).apply { action = ACTION_RESUME }
            val resumePendingIntent = PendingIntent.getService(
                this, REQUEST_CODE_RESUME, resumeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            NotificationCompat.Action.Builder(
                android.R.drawable.ic_media_play,
                "Resume",
                resumePendingIntent
            ).build()
        } else {
            val pauseIntent = Intent(this, TimerService::class.java).apply { action = ACTION_PAUSE }
            val pausePendingIntent = PendingIntent.getService(
                this, REQUEST_CODE_PAUSE, pauseIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            NotificationCompat.Action.Builder(
                android.R.drawable.ic_media_pause,
                "Pause",
                pausePendingIntent
            ).build()
        }

        val resetIntent = Intent(this, TimerService::class.java).apply { action = ACTION_RESET }
        val resetPendingIntent = PendingIntent.getService(
            this, REQUEST_CODE_RESET, resetIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val resetAction = NotificationCompat.Action.Builder(
            android.R.drawable.ic_menu_close_clear_cancel,
            "Reset",
            resetPendingIntent
        ).build()

        val subText = if (isPaused) "Paused" else "Counting down"

        return NotificationCompat.Builder(this, CHANNEL_ID_ONGOING)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("VALUE Timer")
            .setContentText(formattedTime)
            .setSubText(subText)
            .setContentIntent(openAppPendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(pauseResumeAction)
            .addAction(resetAction)
            .build()
    }

    private fun showFinishedNotification() {
        val openAppPendingIntent = createOpenAppPendingIntent()

        val dismissIntent = Intent(this, TimerService::class.java).apply { action = ACTION_DISMISS }
        val dismissPendingIntent = PendingIntent.getService(
            this, REQUEST_CODE_DISMISS, dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val dismissAction = NotificationCompat.Action.Builder(
            android.R.drawable.ic_menu_close_clear_cancel,
            "Dismiss",
            dismissPendingIntent
        ).build()

        val finishedNotification = NotificationCompat.Builder(this, CHANNEL_ID_ALARM)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Time's up!")
            .setContentText("Timer completed")
            .setContentIntent(openAppPendingIntent)
            .setOngoing(true)
            .setAutoCancel(false)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .addAction(dismissAction)
            .build()

        updateNotification(finishedNotification)
    }

    private fun buildNotification(title: String, text: String, isOngoing: Boolean): Notification {
        val openAppPendingIntent = createOpenAppPendingIntent()
        return NotificationCompat.Builder(this, CHANNEL_ID_ONGOING)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(openAppPendingIntent)
            .setOngoing(isOngoing)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createOpenAppPendingIntent(): PendingIntent {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            this,
            REQUEST_CODE_OPEN,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun stopForegroundAndFinish() {
        alarmSoundManager?.stopAlarm()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun acquireWakeLock() {
        try {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "VALUE:TimerWakeLock"
            ).apply {
                acquire(12 * 60 * 60 * 1000L) // 12 hours safety max
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun releaseWakeLock() {
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            wakeLock = null
        }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val ongoingChannel = NotificationChannel(
                CHANNEL_ID_ONGOING,
                "Timer Countdown",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows the active countdown timer progress"
                setShowBadge(false)
            }

            val alarmChannel = NotificationChannel(
                CHANNEL_ID_ALARM,
                "Timer Alarm",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifies when the timer finishes"
                setShowBadge(true)
                enableVibration(true)
            }

            notificationManager.createNotificationChannel(ongoingChannel)
            notificationManager.createNotificationChannel(alarmChannel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        alarmSoundManager?.stopAlarm()
        releaseWakeLock()
        serviceScope.cancel()
    }

    companion object {
        const val NOTIFICATION_ID = 1001

        const val CHANNEL_ID_ONGOING = "timer_ongoing_channel"
        const val CHANNEL_ID_ALARM = "timer_alarm_channel"

        const val ACTION_START = "com.android.value.action.START"
        const val ACTION_PAUSE = "com.android.value.action.PAUSE"
        const val ACTION_RESUME = "com.android.value.action.RESUME"
        const val ACTION_RESET = "com.android.value.action.RESET"
        const val ACTION_DISMISS = "com.android.value.action.DISMISS"
        const val ACTION_FINISH = "com.android.value.action.FINISH"

        private const val REQUEST_CODE_OPEN = 101
        private const val REQUEST_CODE_PAUSE = 102
        private const val REQUEST_CODE_RESUME = 103
        private const val REQUEST_CODE_RESET = 104
        private const val REQUEST_CODE_DISMISS = 105

        fun start(context: Context, durationMillis: Long) {
            val intent = Intent(context, TimerService::class.java).apply {
                action = ACTION_START
            }
            ContextCompat.startForegroundService(context, intent)
        }

        fun pause(context: Context) {
            val intent = Intent(context, TimerService::class.java).apply {
                action = ACTION_PAUSE
            }
            context.startService(intent)
        }

        fun resume(context: Context) {
            val intent = Intent(context, TimerService::class.java).apply {
                action = ACTION_RESUME
            }
            context.startService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, TimerService::class.java).apply {
                action = ACTION_RESET
            }
            context.startService(intent)
        }

        fun finish(context: Context) {
            val intent = Intent(context, TimerService::class.java).apply {
                action = ACTION_FINISH
            }
            context.startService(intent)
        }
    }
}

