package com.example.mytest.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room row for an alarm. Mirrors §4.1 of `alarmx-architecture.md`:
 * - enums are stored as their `name()` (`difficulty`, …) — keeps the schema
 *   stable across version upgrades and human-readable in `adb shell sqlite3`.
 * - [repeatDaysBitmask] packs `Set<DayOfWeek>` into 7 bits
 *   (Mon=1, Tue=2, …, Sun=64) — see [RepeatDaysConverters].
 *
 * Mapping to the domain [com.example.mytest.domain.model.Alarm] is handled by
 * [AlarmMapper] so this class can hold storage-only fields (`createdAt`,
 * `updatedAt`) without polluting the domain.
 */
@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey
    val id: Long,

    @ColumnInfo(name = "trigger_at_epoch_millis")
    val triggerAtEpochMillis: Long,

    val label: String,

    val enabled: Boolean,

    /** [com.example.mytest.domain.model.DifficultyLevel.name]. */
    val difficulty: String,

    @ColumnInfo(name = "snooze_minutes")
    val snoozeMinutes: Int,

    val sound: String,

    @ColumnInfo(name = "repeat_days_bitmask")
    val repeatDaysBitmask: Int,

    @ColumnInfo(name = "created_at")
    val createdAt: Long,

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,
)
