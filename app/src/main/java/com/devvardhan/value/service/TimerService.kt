package com.devvardhan.value.service

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
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.devvardhan.value.MainActivity
import com.devvardhan.value.R
import com.devvardhan.value.core.AlarmSoundManager
import com.devvardhan.value.core.TimeUtils
import com.devvardhan.value.core.TimerEngine
import com.devvardhan.value.model.TimerStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Production-ready Foreground Service that guarantees continuous execution
 * and on-time alarm firing even when the app is backgrounded or screen is turned off.
 */
class TimerService : Service() {

    private var serviceScope: CoroutineScope? = null
    private var observeJob: Job? = null

    private var wakeLock: PowerManager.WakeLock? = null
    private var alarmSoundManager: AlarmSoundManager? = null

    private var lastNotifiedSecond: Long = -1L
    private var lastNotifiedStatus: TimerStatus? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "TimerService onCreate")
        serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
        alarmSoundManager = AlarmSoundManager(this)

        createNotificationChannels()
        acquireWakeLock()
        startObservingTimerEngine()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        Log.d(TAG, "TimerService onStartCommand with action: $action")

        when (action) {
            COMMAND_START -> {
                val initialNotification = buildNotification("VALUE", "Timer running...", false)
                startServiceInForeground(initialNotification)
            }

            COMMAND_PAUSE -> {
                val snapshot = TimerEngine.snapshot.value
                val formatted = TimeUtils.formatHms(snapshot.computeRemainingMillis())
                updateNotification(buildOngoingNotification(formatted, isPaused = true))
            }

            COMMAND_RESUME -> {
                val snapshot = TimerEngine.snapshot.value
                val formatted = TimeUtils.formatHms(snapshot.computeRemainingMillis())
                updateNotification(buildOngoingNotification(formatted, isPaused = false))
            }

            COMMAND_FINISH, ACTION_EXACT_ALARM_TRIGGER -> {
                alarmSoundManager?.startAlarm()
                showFinishedNotification()
            }

            COMMAND_STOP -> {
                stopForegroundAndFinish()
            }

            ACTION_NOTIF_PAUSE -> {
                TimerEngine.pause()
            }

            ACTION_NOTIF_RESUME -> {
                TimerEngine.resume()
            }

            ACTION_NOTIF_RESET -> {
                TimerEngine.reset()
            }

            ACTION_NOTIF_DISMISS -> {
                TimerEngine.dismiss()
            }
        }
        return START_NOT_STICKY
    }

    private fun startObservingTimerEngine() {
        observeJob?.cancel()
        val scope = serviceScope ?: return
        observeJob = scope.launch {
            TimerEngine.snapshot.collectLatest { snapshot ->
                when (snapshot.status) {
                    TimerStatus.Running -> {
                        val remaining = snapshot.computeRemainingMillis()
                        val currentSecond = if (remaining <= 0L) 0L else (remaining + 999L) / 1000L
                        if (currentSecond != lastNotifiedSecond || lastNotifiedStatus != snapshot.status) {
                            lastNotifiedSecond = currentSecond
                            lastNotifiedStatus = snapshot.status
                            val formattedTime = TimeUtils.formatHms(remaining)
                            updateNotification(buildOngoingNotification(formattedTime, isPaused = false))
                        }
                    }

                    TimerStatus.Paused -> {
                        if (lastNotifiedStatus != snapshot.status) {
                            lastNotifiedStatus = snapshot.status
                            val formattedTime = TimeUtils.formatHms(snapshot.computeRemainingMillis())
                            updateNotification(buildOngoingNotification(formattedTime, isPaused = true))
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
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (e: Exception) {
            Log.e(TAG, "startForeground failed", e)
        }
    }

    private fun updateNotification(notification: Notification) {
        try {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(NOTIFICATION_ID, notification)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to update notification", e)
        }
    }

    private fun buildOngoingNotification(formattedTime: String, isPaused: Boolean): Notification {
        val openAppPendingIntent = createOpenAppPendingIntent()

        val pauseResumeAction = if (isPaused) {
            val resumeIntent = Intent(this, TimerService::class.java).apply { action = ACTION_NOTIF_RESUME }
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
            val pauseIntent = Intent(this, TimerService::class.java).apply { action = ACTION_NOTIF_PAUSE }
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

        val resetIntent = Intent(this, TimerService::class.java).apply { action = ACTION_NOTIF_RESET }
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

        val dismissIntent = Intent(this, TimerService::class.java).apply { action = ACTION_NOTIF_DISMISS }
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
            .setFullScreenIntent(openAppPendingIntent, true)
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
        Log.d(TAG, "stopForegroundAndFinish called")
        alarmSoundManager?.stopAlarm()
        try {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } catch (e: Exception) {
            Log.w(TAG, "Error stopping foreground", e)
        }
        stopSelf()
    }

    private fun acquireWakeLock() {
        try {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "VALUE:TimerWakeLock"
            ).apply {
                acquire(12 * 60 * 60 * 1000L) // 12 hours max
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to acquire wake lock", e)
        }
    }

    private fun releaseWakeLock() {
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to release wake lock", e)
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
        Log.d(TAG, "TimerService onDestroy")
        alarmSoundManager?.stopAlarm()
        releaseWakeLock()
        serviceScope?.cancel()
        serviceScope = null
    }

    companion object {
        private const val TAG = "TimerService"
        const val NOTIFICATION_ID = 1001

        const val CHANNEL_ID_ONGOING = "timer_ongoing_channel"
        const val CHANNEL_ID_ALARM = "timer_alarm_channel"

        // Engine -> Service Commands
        const val COMMAND_START = "com.devvardhan.value.cmd.START"
        const val COMMAND_PAUSE = "com.devvardhan.value.cmd.PAUSE"
        const val COMMAND_RESUME = "com.devvardhan.value.cmd.RESUME"
        const val COMMAND_FINISH = "com.devvardhan.value.cmd.FINISH"
        const val COMMAND_STOP = "com.devvardhan.value.cmd.STOP"

        // Notification / Alarm -> Engine Actions
        const val ACTION_NOTIF_PAUSE = "com.devvardhan.value.action.NOTIF_PAUSE"
        const val ACTION_NOTIF_RESUME = "com.devvardhan.value.action.NOTIF_RESUME"
        const val ACTION_NOTIF_RESET = "com.devvardhan.value.action.NOTIF_RESET"
        const val ACTION_NOTIF_DISMISS = "com.devvardhan.value.action.NOTIF_DISMISS"
        const val ACTION_EXACT_ALARM_TRIGGER = "com.devvardhan.value.action.EXACT_ALARM_TRIGGER"

        private const val REQUEST_CODE_OPEN = 101
        private const val REQUEST_CODE_PAUSE = 102
        private const val REQUEST_CODE_RESUME = 103
        private const val REQUEST_CODE_RESET = 104
        private const val REQUEST_CODE_DISMISS = 105

        fun commandStart(context: Context, durationMillis: Long) {
            try {
                val intent = Intent(context, TimerService::class.java).apply {
                    action = COMMAND_START
                }
                ContextCompat.startForegroundService(context, intent)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start foreground service", e)
            }
        }

        fun commandPause(context: Context) {
            try {
                val intent = Intent(context, TimerService::class.java).apply {
                    action = COMMAND_PAUSE
                }
                context.startService(intent)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to send pause command", e)
            }
        }

        fun commandResume(context: Context) {
            try {
                val intent = Intent(context, TimerService::class.java).apply {
                    action = COMMAND_RESUME
                }
                context.startService(intent)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to send resume command", e)
            }
        }

        fun commandFinish(context: Context) {
            try {
                val intent = Intent(context, TimerService::class.java).apply {
                    action = COMMAND_FINISH
                }
                context.startService(intent)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to send finish command", e)
            }
        }

        fun commandStop(context: Context) {
            try {
                val intent = Intent(context, TimerService::class.java).apply {
                    action = COMMAND_STOP
                }
                context.startService(intent)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to send stop command", e)
            }
        }
    }
}
