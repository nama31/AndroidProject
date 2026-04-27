package com.example.mytest.system.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.mytest.MainActivity
import com.example.mytest.R

/**
 * Foreground service that plays the alarm ringtone, holds a partial
 * WakeLock, and shows a full-screen-intent notification so the dismiss UI
 * appears even on a locked device.
 *
 * Lifecycle:
 *   - `START`  (no action set): start foreground, acquire WakeLock, start ringtone.
 *   - `STOP`   (`AlarmIntents.ACTION_STOP_RINGING`): release everything and `stopSelf`.
 *
 * The service stays alive for the entire dismissal interaction; tearing it
 * down is the responsibility of [com.example.mytest.system.alarm.AndroidRingingController]
 * (called by the ViewModel on a correct answer or snooze).
 */
class AlarmRingtoneService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var currentAlarmId: Long = -1L

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        ensureChannel(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            AlarmIntents.ACTION_STOP_RINGING -> {
                Log.d(TAG, "Stop requested")
                stopRingingAndSelf()
                return START_NOT_STICKY
            }
        }

        val alarmId = intent?.getLongExtra(AlarmIntents.EXTRA_ALARM_ID, -1L) ?: -1L
        if (alarmId == -1L) {
            Log.w(TAG, "Started with no EXTRA_ALARM_ID, stopping")
            stopSelf()
            return START_NOT_STICKY
        }
        currentAlarmId = alarmId

        startInForeground(alarmId)
        acquireWakeLock()
        startRingtone()
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        stopRingtone()
        releaseWakeLock()
        super.onDestroy()
    }

    // ----------------------------------------------------------------------
    // Foreground notification
    // ----------------------------------------------------------------------

    private fun startInForeground(alarmId: Long) {
        val fullScreenIntent = Intent(this, MainActivity::class.java).apply {
            action = AlarmIntents.ACTION_SHOW_DISMISS
            putExtra(AlarmIntents.EXTRA_ALARM_ID, alarmId)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val fullScreenPi = PendingIntent.getActivity(
            this,
            alarmId.toInt(),
            fullScreenIntent,
            pendingIntentFlags(),
        )

        val notification = NotificationCompat.Builder(this, AlarmIntents.CHANNEL_ID_ALARM)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(getString(R.string.app_name))
            .setContentText("Alarm — tap to dismiss")
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(fullScreenPi)
            .setFullScreenIntent(fullScreenPi, /* highPriority = */ true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                AlarmIntents.NOTIFICATION_ID_ALARM,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK,
            )
        } else {
            startForeground(AlarmIntents.NOTIFICATION_ID_ALARM, notification)
        }
    }

    // ----------------------------------------------------------------------
    // Ringtone
    // ----------------------------------------------------------------------

    private fun startRingtone() {
        val uri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            ?: return
        try {
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build(),
                )
                setDataSource(this@AlarmRingtoneService, uri)
                isLooping = true
                prepare()
                start()
            }
            Log.d(TAG, "Ringtone started")
        } catch (t: Throwable) {
            Log.w(TAG, "Failed to start ringtone", t)
        }
    }

    private fun stopRingtone() {
        try {
            mediaPlayer?.apply {
                if (isPlaying) stop()
                release()
            }
        } catch (t: Throwable) {
            Log.w(TAG, "Failed to stop ringtone", t)
        } finally {
            mediaPlayer = null
        }
    }

    // ----------------------------------------------------------------------
    // WakeLock
    // ----------------------------------------------------------------------

    private fun acquireWakeLock() {
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        val lock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "AlarmX:Service").apply {
            setReferenceCounted(false)
        }
        try {
            lock.acquire(MAX_RING_DURATION_MS)
        } catch (t: Throwable) {
            Log.w(TAG, "WakeLock acquire failed", t)
        }
        wakeLock = lock
    }

    private fun releaseWakeLock() {
        try {
            wakeLock?.takeIf { it.isHeld }?.release()
        } catch (t: Throwable) {
            Log.w(TAG, "WakeLock release failed", t)
        } finally {
            wakeLock = null
        }
    }

    // ----------------------------------------------------------------------
    // Teardown
    // ----------------------------------------------------------------------

    private fun stopRingingAndSelf() {
        stopRingtone()
        releaseWakeLock()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun pendingIntentFlags(): Int {
        return PendingIntent.FLAG_UPDATE_CURRENT or
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_IMMUTABLE else 0
    }

    companion object {
        private const val TAG = "AlarmRingtoneService"

        /** WakeLock cap so a runaway service can't drain the battery. */
        private const val MAX_RING_DURATION_MS = 10L * 60L * 1000L // 10 minutes

        /**
         * Idempotent — safe to call from `Application.onCreate()` and from
         * the service itself.
         */
        fun ensureChannel(context: Context) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
            val nm = context.getSystemService(NotificationManager::class.java) ?: return
            if (nm.getNotificationChannel(AlarmIntents.CHANNEL_ID_ALARM) != null) return

            val channel = NotificationChannel(
                AlarmIntents.CHANNEL_ID_ALARM,
                "Alarms",
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = "Firing alarm clock — full-screen dismiss UI."
                setBypassDnd(true)
                lockscreenVisibility = NotificationManager.IMPORTANCE_HIGH
                // No channel sound — ringtone is owned by the service so we
                // can stop it on a correct answer.
                setSound(null, null)
                enableVibration(true)
            }
            nm.createNotificationChannel(channel)
        }
    }
}
