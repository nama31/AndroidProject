package com.example.mytest.data.db

import com.example.mytest.domain.model.Alarm
import com.example.mytest.domain.model.DifficultyLevel

/**
 * `Alarm` ↔ `AlarmEntity` mapping.
 *
 * Entity-only fields (`createdAt`, `updatedAt`) are managed here so the
 * domain stays free of storage concerns. `now` is injected so callers can
 * supply a deterministic clock in tests.
 */
object AlarmMapper {

    fun AlarmEntity.toDomain(): Alarm = Alarm(
        id = id,
        triggerAtEpochMillis = triggerAtEpochMillis,
        label = label,
        enabled = enabled,
        difficulty = parseDifficulty(difficulty),
        snoozeMinutes = snoozeMinutes,
        sound = sound,
        repeatDays = RepeatDaysConverters.fromBitmask(repeatDaysBitmask),
    )

    fun Alarm.toEntity(
        existing: AlarmEntity? = null,
        now: Long = System.currentTimeMillis(),
    ): AlarmEntity = AlarmEntity(
        id = id,
        triggerAtEpochMillis = triggerAtEpochMillis,
        label = label,
        enabled = enabled,
        difficulty = difficulty.name,
        snoozeMinutes = snoozeMinutes,
        sound = sound,
        repeatDaysBitmask = RepeatDaysConverters.toBitmask(repeatDays),
        createdAt = existing?.createdAt ?: now,
        updatedAt = now,
    )

    private fun parseDifficulty(raw: String): DifficultyLevel =
        runCatching { DifficultyLevel.valueOf(raw) }
            .getOrDefault(DifficultyLevel.MEDIUM)
}
