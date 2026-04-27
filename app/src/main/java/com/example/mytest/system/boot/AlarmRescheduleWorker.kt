package com.example.mytest.system.boot

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.mytest.domain.repository.AlarmRepository
import com.example.mytest.domain.scheduler.AlarmScheduler
import com.example.mytest.domain.util.AlarmScheduling
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Background worker that re-arms every enabled alarm after a reboot or app
 * upgrade.
 *
 * - Alarms still in the future → re-scheduled as-is.
 * - Past-due repeating alarms → advanced to the next occurrence via
 *   [AlarmRepository.save] (which handles the reschedule).
 * - Past-due one-shots → disabled. Firing them late (possibly many hours
 *   after the intended time) is worse than requiring the user to re-arm.
 *
 * Constructor-injected via `@HiltWorker`; see
 * [com.example.mytest.AlarmXApp.workManagerConfiguration] for the factory
 * hook-up.
 */
@HiltWorker
class AlarmRescheduleWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val alarmRepository: AlarmRepository,
    private val alarmScheduler: AlarmScheduler,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val alarms = alarmRepository.list()
            val now = System.currentTimeMillis()
            var armed = 0
            var advanced = 0
            var disabled = 0
            for (alarm in alarms) {
                if (!alarm.enabled) continue
                if (alarm.triggerAtEpochMillis > now) {
                    alarmScheduler.schedule(alarm)
                    armed++
                    continue
                }
                if (alarm.repeatDays.isEmpty()) {
                    alarmRepository.setEnabled(alarm.id, enabled = false)
                    disabled++
                    continue
                }
                val next = AlarmScheduling.advanceRepeating(alarm) ?: continue
                alarmRepository.save(next)
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
