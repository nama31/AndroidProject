package com.example.mytest.data.repository

import com.example.mytest.data.db.AlarmDao
import com.example.mytest.data.db.AlarmMapper.toDomain
import com.example.mytest.data.db.AlarmMapper.toEntity
import com.example.mytest.domain.model.Alarm
import com.example.mytest.domain.repository.AlarmRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * **Transitional** [AlarmRepository] implementation.
 *
 * This wraps [AlarmDao] and persists alarms to Room — but it does NOT call
 * an [com.example.mytest.domain.scheduler.AlarmScheduler] yet, so saved
 * alarms will not actually fire on the OS. The full implementation lives in
 * the system-layer PR (AlarmManager + receivers + foreground service).
 *
 * Until then, this class is enough to wire [com.example.mytest.ui.alarm.list.AlarmListScreen]
 * end-to-end (list/create/toggle/delete persist correctly across launches).
 */
class LocalAlarmRepository(
    private val alarmDao: AlarmDao,
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
    }

    override suspend fun dismiss(alarmId: Long) {
        alarmDao.setEnabled(alarmId, enabled = false, updatedAt = System.currentTimeMillis())
    }

    override suspend fun delete(alarmId: Long) {
        alarmDao.deleteById(alarmId)
    }

    override suspend fun setEnabled(alarmId: Long, enabled: Boolean) {
        alarmDao.setEnabled(alarmId, enabled, updatedAt = System.currentTimeMillis())
    }
}
