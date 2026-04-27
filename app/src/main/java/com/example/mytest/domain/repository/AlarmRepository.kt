package com.example.mytest.domain.repository

import com.example.mytest.domain.model.Alarm
import kotlinx.coroutines.flow.Flow

/**
 * Domain-level contract for the alarm store.
 *
 * Implementations live in `data/repository/` and combine an [AlarmDao] with
 * an [com.example.mytest.domain.scheduler.AlarmScheduler] so that persisting
 * an alarm and (un)scheduling it remain a single atomic operation. This
 * interface is intentionally framework-free — UI/domain code depends only on
 * this contract, never on Room or `AlarmManager`.
 */
interface AlarmRepository {

    /** Reactive stream of every persisted alarm, ordered by trigger time. */
    fun observeAlarms(): Flow<List<Alarm>>

    /** One-shot list of every persisted alarm. */
    suspend fun list(): List<Alarm>

    /** Look up a single alarm by id, or `null` if it doesn't exist. */
    suspend fun findById(id: Long): Alarm?

    /**
     * Insert or update [alarm]. If [Alarm.enabled] is `true`, the
     * implementation MUST also schedule it; otherwise it MUST cancel any
     * pending schedule.
     */
    suspend fun save(alarm: Alarm)

    /**
     * Mark the alarm as fired and disable it (one-shot semantics). Repeating
     * alarms are re-armed for the next occurrence by the system layer.
     */
    suspend fun dismiss(alarmId: Long)

    /** Permanently remove the alarm and cancel its schedule. */
    suspend fun delete(alarmId: Long)

    /** Toggle the enabled flag and (un)schedule accordingly. */
    suspend fun setEnabled(alarmId: Long, enabled: Boolean)
}
