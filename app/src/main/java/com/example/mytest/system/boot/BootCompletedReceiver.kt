package com.example.mytest.system.boot

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

/**
 * Listens for `BOOT_COMPLETED` and `LOCKED_BOOT_COMPLETED` and enqueues an
 * [AlarmRescheduleWorker] to re-arm every enabled alarm.
 *
 * `LOCKED_BOOT_COMPLETED` (API 24+) lets us start the work before the user
 * unlocks the device — important when the first alarm of the morning needs
 * to fire before login.
 *
 * The receiver itself does almost nothing — WorkManager keeps the work
 * alive across process death.
 */
class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_LOCKED_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_TIME_CHANGED,
            -> {
                Log.d(TAG, "Boot/replace/time signal: ${intent.action}")
                val request = OneTimeWorkRequestBuilder<AlarmRescheduleWorker>().build()
                WorkManager.getInstance(context).enqueueUniqueWork(
                    UNIQUE_WORK_NAME,
                    ExistingWorkPolicy.REPLACE,
                    request,
                )
            }
        }
    }

    companion object {
        private const val TAG = "BootCompletedRecv"
        const val UNIQUE_WORK_NAME = "alarmx.reschedule_all"
    }
}
