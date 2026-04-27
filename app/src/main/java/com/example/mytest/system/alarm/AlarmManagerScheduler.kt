package com.example.mytest.system.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.mytest.MainActivity
import com.example.mytest.domain.model.Alarm
import com.example.mytest.domain.scheduler.AlarmScheduler
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * `AlarmScheduler` implementation backed by [AlarmManager].
 *
 * Uses [AlarmManager.setAlarmClock] — the canonical API for user-facing
 * alarms:
 *   - always fires at the exact time, even in Doze / app-standby,
 *   - shows up in the system clock UI / status bar alarm icon,
 *   - is exempt from `SCHEDULE_EXACT_ALARM` runtime grant on API 31+ when the
 *     calling app is the user's chosen alarm clock.
 *
 * For environments where `setAlarmClock` is unavailable (it isn't, since
 * API 21, but kept defensive) we fall back to
 * [AlarmManager.setExactAndAllowWhileIdle].
 *
 * Repeat-day handling is **not** implemented in this PR — the scheduler arms
 * a single one-shot trigger using [Alarm.triggerAtEpochMillis]. The
 * `Reschedule` worker handles re-arming on boot.
 */
@Singleton
class AlarmManagerScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) : AlarmScheduler {

    private val alarmManager: AlarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun schedule(alarm: Alarm) {
        if (!alarm.enabled) {
            cancel(alarm.id)
            return
        }

        val operation = operationPendingIntent(alarm.id, alarm.sound, mutable = false)
        val showIntent = showPendingIntent(alarm.id)

        try {
            val info = AlarmManager.AlarmClockInfo(alarm.triggerAtEpochMillis, showIntent)
            alarmManager.setAlarmClock(info, operation)
            Log.d(TAG, "Scheduled alarm id=${alarm.id} at=${alarm.triggerAtEpochMillis}")
        } catch (security: SecurityException) {
            // Some OEMs / API 31+ devices can deny exact alarms even for
            // alarm-clock category. Fall back to inexact-but-while-idle so
            // the alarm at least eventually fires.
            Log.w(TAG, "setAlarmClock denied, falling back to setExactAndAllowWhileIdle", security)
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                alarm.triggerAtEpochMillis,
                operation,
            )
        }
    }

    override fun cancel(alarmId: Long) {
        val operation = operationPendingIntent(alarmId, sound = null, mutable = false)
        alarmManager.cancel(operation)
        operation.cancel()
        Log.d(TAG, "Cancelled alarm id=$alarmId")
    }

    private fun operationPendingIntent(
        alarmId: Long,
        sound: String?,
        mutable: Boolean,
    ): PendingIntent {
        val intent = Intent(context, AlarmBroadcastReceiver::class.java).apply {
            action = AlarmIntents.ACTION_ALARM_FIRED
            putExtra(AlarmIntents.EXTRA_ALARM_ID, alarmId)
            if (sound != null) putExtra(AlarmIntents.EXTRA_ALARM_SOUND, sound)
            // Make the Intent unique per alarm id so PendingIntents don't collide.
            data = android.net.Uri.parse("alarmx://alarm/$alarmId")
        }
        val flags = pendingIntentFlags(mutable)
        return PendingIntent.getBroadcast(context, alarmId.toInt(), intent, flags)
    }

    private fun showPendingIntent(alarmId: Long): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            action = AlarmIntents.ACTION_SHOW_DISMISS
            putExtra(AlarmIntents.EXTRA_ALARM_ID, alarmId)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val flags = pendingIntentFlags(mutable = false)
        return PendingIntent.getActivity(context, alarmId.toInt(), intent, flags)
    }

    private fun pendingIntentFlags(mutable: Boolean): Int {
        val base = PendingIntent.FLAG_UPDATE_CURRENT
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            base or if (mutable) PendingIntent.FLAG_MUTABLE else PendingIntent.FLAG_IMMUTABLE
        } else {
            base
        }
    }

    companion object {
        private const val TAG = "AlarmManagerScheduler"
    }
}
