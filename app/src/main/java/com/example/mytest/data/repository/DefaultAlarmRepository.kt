package com.example.mytest.data.repository

import com.example.mytest.data.db.AlarmDao
import com.example.mytest.data.db.AlarmMapper.toDomain
import com.example.mytest.data.db.AlarmMapper.toEntity
import com.example.mytest.domain.model.Alarm
import com.example.mytest.domain.repository.AlarmRepository
import com.example.mytest.domain.scheduler.AlarmScheduler
import com.example.mytest.domain.util.AlarmScheduling
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Production [AlarmRepository] implementation. Persists to Room via
 * [AlarmDao] and keeps the OS-level schedule in lock-step via
 * [AlarmScheduler]:
 *
 *   - `save(enabled = true)`  → upsert + `scheduler.schedule(alarm)`
 *   - `save(enabled = false)` → upsert + `scheduler.cancel(alarmId)`
 *   - `dismiss(id)`:
 *       - one-shot (`repeatDays.isEmpty()`) → flip `enabled = false` +
 *         `scheduler.cancel`.
 *       - repeating → advance `triggerAtEpochMillis` to the next occurrence
 *         and reschedule; the alarm stays enabled.
 *   - `setEnabled(id, false)` → DB update + `scheduler.cancel`
 *   - `setEnabled(id, true)`  → DB update + `scheduler.schedule`
 *   - `delete(id)`            → row delete + `scheduler.cancel`
 *
 * Replaces the transitional `LocalAlarmRepository` from the screens PR.
 */
@Singleton
class DefaultAlarmRepository @Inject constructor(
    private val alarmDao: AlarmDao,
    private val scheduler: AlarmScheduler,
) : AlarmRepository {

    override fun observeAlarms(): Flow<List<Alarm>> =
        alarmDao.observeAll().map { rows -> rows.map { it.toDomain() } }

    override suspend fun list(): List<Alarm> =
        alarmDao.getAll().map { it.toDomain() }

    override suspend fun findById(id: Long): Alarm? =
        alarmDao.findById(id)?.toDomain()

    override suspend fun save(alarm: Alarm) {
        val existing = alarmDao.findById(alarm.id)
        alarmDao.upsert(alarm.toEntity(existing = existing))
        if (alarm.enabled) {
            scheduler.schedule(alarm)
        } else {
            scheduler.cancel(alarm.id)
        }
    }

    override suspend fun dismiss(alarmId: Long) {
        val existing = alarmDao.findById(alarmId)?.toDomain()
        if (existing != null && existing.repeatDays.isNotEmpty()) {
            // Repeating alarm — advance to the next occurrence rather than
            // turning the alarm off. save() handles the reschedule.
            val advanced = AlarmScheduling.advanceRepeating(existing)
            if (advanced != null) {
                save(advanced)
                return
            }
        }
        alarmDao.setEnabled(alarmId, enabled = false, updatedAt = System.currentTimeMillis())
        scheduler.cancel(alarmId)
    }

    override suspend fun delete(alarmId: Long) {
        alarmDao.deleteById(alarmId)
        scheduler.cancel(alarmId)
    }

    override suspend fun setEnabled(alarmId: Long, enabled: Boolean) {
        alarmDao.setEnabled(alarmId, enabled, updatedAt = System.currentTimeMillis())
        if (enabled) {
            val alarm = alarmDao.findById(alarmId)?.toDomain() ?: return
            scheduler.schedule(alarm)
        } else {
            scheduler.cancel(alarmId)
        }
    }
}
