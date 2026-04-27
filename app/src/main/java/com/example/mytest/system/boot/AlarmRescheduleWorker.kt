package com.example.mytest.system.boot

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.mytest.AppGraph
import com.example.mytest.domain.util.AlarmScheduling

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
            val repo = AppGraph.alarmRepository
            val scheduler = AppGraph.alarmScheduler
            val alarms = repo.list()
            val now = System.currentTimeMillis()
            var armed = 0
            var advanced = 0
            var disabled = 0
            for (alarm in alarms) {
                if (!alarm.enabled) continue
                if (alarm.triggerAtEpochMillis > now) {
                    // Still in the future — re-arm as-is.
                    scheduler.schedule(alarm)
                    armed++
                    continue
                }
                // Past-due while the device was off.
                if (alarm.repeatDays.isEmpty()) {
                    // One-shot that we missed during the downtime — disable
                    // and leave it to the user to re-arm. Firing late would
                    // be worse than not firing at all.
                    repo.setEnabled(alarm.id, enabled = false)
                    disabled++
                    continue
                }
                val next = AlarmScheduling.advanceRepeating(alarm) ?: continue
                repo.save(next)
                advanced++
            }
            Log.d(TAG, "Reschedule: armed=$armed advanced=$advanced disabled=$disabled")
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
