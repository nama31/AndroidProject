package com.example.mytest.system.boot

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.mytest.AppGraph

/**
 * Background worker that re-arms every enabled alarm after a reboot or app
 * upgrade.
 *
 * Resolves dependencies through [AppGraph] (the manual service-locator) so
 * we don't need a custom `WorkerFactory` until Hilt-Work lands.
 */
class AlarmRescheduleWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val alarms = AppGraph.alarmRepository.list()
            val scheduler = AppGraph.alarmScheduler
            val now = System.currentTimeMillis()
            var armed = 0
            for (alarm in alarms) {
                if (!alarm.enabled) continue
                if (alarm.triggerAtEpochMillis <= now) continue
                scheduler.schedule(alarm)
                armed++
            }
            Log.d(TAG, "Rescheduled $armed alarm(s)")
            Result.success()
        } catch (t: Throwable) {
            Log.e(TAG, "Reschedule worker failed", t)
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "AlarmRescheduleWorker"
    }
}
