package com.example.mytest.system.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.util.Log

/**
 * Receives the `PendingIntent` that [AlarmManagerScheduler] hands to
 * `AlarmManager`.
 *
 * Responsibilities:
 *   1. Acquire a short partial WakeLock so the device stays awake long
 *      enough to start the foreground service.
 *   2. Forward the `EXTRA_ALARM_ID` to [AlarmRingtoneService] via
 *      `startForegroundService`.
 *
 * The receiver itself does NOT play sound or hold a long WakeLock — that
 * lives in [AlarmRingtoneService] so it survives `onReceive` returning.
 */
class AlarmBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != AlarmIntents.ACTION_ALARM_FIRED) return

        val alarmId = intent.getLongExtra(AlarmIntents.EXTRA_ALARM_ID, -1L)
        if (alarmId == -1L) {
            Log.w(TAG, "Received alarm intent without EXTRA_ALARM_ID")
            return
        }
        Log.d(TAG, "Alarm fired: id=$alarmId")

        // Brief wakelock — released as soon as the service is up and acquires
        // its own.
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = pm.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "AlarmX:Receiver",
        ).apply { setReferenceCounted(false) }
        try {
            wakeLock.acquire(WAKELOCK_TIMEOUT_MS)
        } catch (t: Throwable) {
            Log.w(TAG, "Wakelock acquisition failed", t)
        }

        val serviceIntent = Intent(context, AlarmRingtoneService::class.java).apply {
            putExtra(AlarmIntents.EXTRA_ALARM_ID, alarmId)
            intent.getStringExtra(AlarmIntents.EXTRA_ALARM_SOUND)?.let { sound ->
                putExtra(AlarmIntents.EXTRA_ALARM_SOUND, sound)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }

        // Release the receiver wakelock — the service now holds its own.
        try {
            if (wakeLock.isHeld) wakeLock.release()
        } catch (t: Throwable) {
            Log.w(TAG, "Wakelock release failed", t)
        }
    }

    companion object {
        private const val TAG = "AlarmBroadcastRecv"
        private const val WAKELOCK_TIMEOUT_MS = 10_000L
    }
}
