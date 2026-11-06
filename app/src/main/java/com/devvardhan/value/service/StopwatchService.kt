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
import com.devvardhan.value.core.StopwatchEngine
import com.devvardhan.value.core.TimeUtils
import com.devvardhan.value.model.StopwatchStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Foreground Service for the Stopwatch.
 * Uses an OS-level chronometer notification (setUsesChronometer / setWhen)
 * so Android's SystemUI natively paints live elapsed time without waking up CPU every second.
 */
class StopwatchService : Service() {

    private var serviceScope: CoroutineScope? = null
    private var observeJob: Job? = null
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "StopwatchService onCreate")
        serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

        createNotificationChannel()
        acquireWakeLock()
        startObservingStopwatch()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        Log.d(TAG, "StopwatchService onStartCommand: $action")

        when (action) {
            COMMAND_START -> {
                val snapshot = StopwatchEngine.snapshot.value
                val notification = buildChronometerNotification(snapshot.accumulatedMillis, isRunning = true)
                startServiceInForeground(notification)
            }

            COMMAND_PAUSE -> {
                val snapshot = StopwatchEngine.snapshot.value
                updateNotification(buildChronometerNotification(snapshot.accumulatedMillis, isRunning = false))
            }

            COMMAND_RESUME -> {
                val snapshot = StopwatchEngine.snapshot.value
                updateNotification(buildChronometerNotification(snapshot.accumulatedMillis, isRunning = true))
            }

            COMMAND_STOP -> {
                stopForegroundAndFinish()
            }

            ACTION_NOTIF_PAUSE -> {
                StopwatchEngine.pause()
            }

            ACTION_NOTIF_RESUME -> {
                StopwatchEngine.resume()
            }

            ACTION_NOTIF_LAP -> {
                StopwatchEngine.lap()
            }

            ACTION_NOTIF_RESET -> {
                StopwatchEngine.reset()
            }
        }

        return START_NOT_STICKY
    }

    private fun startObservingStopwatch() {
        observeJob?.cancel()
        val scope = serviceScope ?: return
        observeJob = scope.launch {
            StopwatchEngine.snapshot.collectLatest { snapshot ->
                when (snapshot.status) {
                    StopwatchStatus.Running -> {
                        updateNotification(buildChronometerNotification(snapshot.accumulatedMillis, isRunning = true))
                    }

                    StopwatchStatus.Paused -> {
                        updateNotification(buildChronometerNotification(snapshot.accumulatedMillis, isRunning = false))
                    }

                    StopwatchStatus.Idle -> {
                        stopForegroundAndFinish()
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
            Log.e(TAG, "startForeground failed for StopwatchService", e)
        }
    }

    private fun updateNotification(notification: Notification) {
        try {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(NOTIFICATION_ID, notification)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to update stopwatch notification", e)
        }
    }

    private fun buildChronometerNotification(accumulatedMillis: Long, isRunning: Boolean): Notification {
        val openAppPendingIntent = createOpenAppPendingIntent()

        val pauseResumeAction = if (isRunning) {
            val pauseIntent = Intent(this, StopwatchService::class.java).apply { action = ACTION_NOTIF_PAUSE }
            val pausePendingIntent = PendingIntent.getService(
                this, REQUEST_CODE_PAUSE, pauseIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            NotificationCompat.Action.Builder(
                android.R.drawable.ic_media_pause,
                "Pause",
                pausePendingIntent
            ).build()
        } else {
            val resumeIntent = Intent(this, StopwatchService::class.java).apply { action = ACTION_NOTIF_RESUME }
            val resumePendingIntent = PendingIntent.getService(
                this, REQUEST_CODE_RESUME, resumeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            NotificationCompat.Action.Builder(
                android.R.drawable.ic_media_play,
                "Resume",
                resumePendingIntent
            ).build()
        }

        val lapIntent = Intent(this, StopwatchService::class.java).apply { action = ACTION_NOTIF_LAP }
        val lapPendingIntent = PendingIntent.getService(
            this, REQUEST_CODE_LAP, lapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val lapAction = NotificationCompat.Action.Builder(
            android.R.drawable.ic_input_add,
            "Lap",
            lapPendingIntent
        ).build()

        val resetIntent = Intent(this, StopwatchService::class.java).apply { action = ACTION_NOTIF_RESET }
        val resetPendingIntent = PendingIntent.getService(
            this, REQUEST_CODE_RESET, resetIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val resetAction = NotificationCompat.Action.Builder(
            android.R.drawable.ic_menu_close_clear_cancel,
            "Reset",
            resetPendingIntent
        ).build()

        val builder = NotificationCompat.Builder(this, CHANNEL_ID_STOPWATCH)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("VALUE Stopwatch")
            .setContentIntent(openAppPendingIntent)
            .setOngoing(isRunning)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(pauseResumeAction)

        if (isRunning) {
            builder.addAction(lapAction)
            builder.setUsesChronometer(true)
            builder.setWhen(System.currentTimeMillis() - accumulatedMillis)
            builder.setShowWhen(true)
            builder.setSubText("Running")
        } else {
            builder.setUsesChronometer(false)
            builder.setShowWhen(false)
            builder.setContentText(TimeUtils.formatStopwatchFull(accumulatedMillis))
            builder.setSubText("Paused")
            builder.addAction(resetAction)
        }

        return builder.build()
    }

    private fun createOpenAppPendingIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_TARGET_MODE, "STOPWATCH")
        }
        return PendingIntent.getActivity(
            this,
            REQUEST_CODE_OPEN,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun stopForegroundAndFinish() {
        Log.d(TAG, "Stopping StopwatchService")
        try {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } catch (e: Exception) {
            Log.w(TAG, "Error stopping foreground", e)
        }
        stopSelf()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID_STOPWATCH,
                "Stopwatch",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Ongoing stopwatch chronometer and actions"
                setShowBadge(false)
                setSound(null, null)
                enableVibration(false)
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun acquireWakeLock() {
        try {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "VALUE:StopwatchWakeLock"
            ).apply {
                setReferenceCounted(false)
                acquire(10 * 60 * 1000L) // 10 minutes timeout
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to acquire wake lock for stopwatch", e)
        }
    }

    private fun releaseWakeLock() {
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to release wake lock", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "StopwatchService onDestroy")
        releaseWakeLock()
        serviceScope?.cancel()
        serviceScope = null
    }

    companion object {
        private const val TAG = "StopwatchService"
        const val NOTIFICATION_ID = 1002
        const val CHANNEL_ID_STOPWATCH = "stopwatch_channel"
        const val EXTRA_TARGET_MODE = "extra_target_mode"

        // Engine -> Service Commands
        const val COMMAND_START = "com.devvardhan.value.sw.START"
        const val COMMAND_PAUSE = "com.devvardhan.value.sw.PAUSE"
        const val COMMAND_RESUME = "com.devvardhan.value.sw.RESUME"
        const val COMMAND_STOP = "com.devvardhan.value.sw.STOP"

        // Notification -> Engine Actions
        const val ACTION_NOTIF_PAUSE = "com.devvardhan.value.sw.NOTIF_PAUSE"
        const val ACTION_NOTIF_RESUME = "com.devvardhan.value.sw.NOTIF_RESUME"
        const val ACTION_NOTIF_LAP = "com.devvardhan.value.sw.NOTIF_LAP"
        const val ACTION_NOTIF_RESET = "com.devvardhan.value.sw.NOTIF_RESET"

        private const val REQUEST_CODE_OPEN = 201
        private const val REQUEST_CODE_PAUSE = 202
        private const val REQUEST_CODE_RESUME = 203
        private const val REQUEST_CODE_LAP = 204
        private const val REQUEST_CODE_RESET = 205

        fun commandStart(context: Context, accumulatedMillis: Long) {
            try {
                val intent = Intent(context, StopwatchService::class.java).apply {
                    action = COMMAND_START
                }
                ContextCompat.startForegroundService(context, intent)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start stopwatch foreground service", e)
            }
        }

        fun commandPause(context: Context, accumulatedMillis: Long) {
            try {
                val intent = Intent(context, StopwatchService::class.java).apply {
                    action = COMMAND_PAUSE
                }
                context.startService(intent)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to send pause command to stopwatch", e)
            }
        }

        fun commandResume(context: Context, accumulatedMillis: Long) {
            try {
                val intent = Intent(context, StopwatchService::class.java).apply {
                    action = COMMAND_RESUME
                }
                context.startService(intent)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to send resume command to stopwatch", e)
            }
        }

        fun commandStop(context: Context) {
            try {
                val intent = Intent(context, StopwatchService::class.java).apply {
                    action = COMMAND_STOP
                }
                context.startService(intent)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to send stop command to stopwatch", e)
            }
        }
    }
}

