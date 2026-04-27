package com.example.mytest.data.repository

import com.example.mytest.data.db.AlarmDao
import com.example.mytest.data.db.AlarmMapper.toDomain
import com.example.mytest.data.db.AlarmMapper.toEntity
import com.example.mytest.domain.model.Alarm
import com.example.mytest.domain.repository.AlarmRepository
import com.example.mytest.domain.scheduler.AlarmScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Production [AlarmRepository] implementation. Persists to Room via
 * [AlarmDao] and keeps the OS-level schedule in lock-step via
 * [AlarmScheduler]:
 *
 *   - `save(enabled = true)`  → upsert + `scheduler.schedule(alarm)`
 *   - `save(enabled = false)` → upsert + `scheduler.cancel(alarmId)`
 *   - `dismiss(id)`           → flip `enabled = false` + `scheduler.cancel`
 *   - `setEnabled(id, false)` → DB update + `scheduler.cancel`
 *   - `setEnabled(id, true)`  → DB update + `scheduler.schedule`
 *   - `delete(id)`            → row delete + `scheduler.cancel`
 *
 * Replaces the transitional `LocalAlarmRepository` from the screens PR.
 */
class DefaultAlarmRepository(
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
