package com.example.mytest.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Room access object for [AlarmEntity].
 *
 * Reads expose [Flow] so the UI layer can stay reactive without polling.
 * Writes are `suspend` — Room dispatches them to its IO pool.
 */
@Dao
interface AlarmDao {

    /** Reactive list ordered by next firing time. */
    @Query("SELECT * FROM alarms ORDER BY trigger_at_epoch_millis ASC")
    fun observeAll(): Flow<List<AlarmEntity>>

    /** Reactive single-row stream, emits `null` after deletion. */
    @Query("SELECT * FROM alarms WHERE id = :id LIMIT 1")
    fun observeById(id: Long): Flow<AlarmEntity?>

    @Query("SELECT * FROM alarms ORDER BY trigger_at_epoch_millis ASC")
    suspend fun getAll(): List<AlarmEntity>

    @Query("SELECT * FROM alarms WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): AlarmEntity?

    @Query("SELECT * FROM alarms WHERE enabled = 1")
    suspend fun getAllEnabled(): List<AlarmEntity>

    /** Insert or replace. Used both for create and update flows. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(alarm: AlarmEntity)

    @Query("UPDATE alarms SET enabled = :enabled, updated_at = :updatedAt WHERE id = :id")
    suspend fun setEnabled(id: Long, enabled: Boolean, updatedAt: Long)

    @Query("DELETE FROM alarms WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM alarms")
    suspend fun deleteAll()
}
