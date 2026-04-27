package com.example.mytest.domain.model

import java.time.DayOfWeek

/**
 * Domain representation of a single alarm.
 *
 * Pure Kotlin: no Room / Android annotations leak into this layer. Maps to
 * [com.example.mytest.data.db.AlarmEntity] via
 * [com.example.mytest.data.db.AlarmMapper].
 *
 * @property id stable identifier supplied by the caller (used as the
 *   `AlarmManager` request code in the system layer).
 * @property triggerAtEpochMillis next firing time in epoch milliseconds.
 * @property label human-friendly label shown in the list and on the Dismiss
 *   screen ("Workout", "Standup", …).
 * @property enabled whether the alarm is currently armed.
 * @property difficulty cognitive challenge difficulty applied when the alarm
 *   fires.
 * @property snoozeMinutes how far ahead snoozing pushes the next trigger.
 * @property sound ringtone identifier (e.g. URI string or built-in name).
 * @property repeatDays days of week on which this alarm repeats; an empty set
 *   means a one-shot alarm. Persisted as a bitmask — see
 *   [com.example.mytest.data.db.RepeatDaysConverters].
 */
data class Alarm(
    val id: Long,
    val triggerAtEpochMillis: Long,
    val label: String = "",
    val enabled: Boolean = true,
    val difficulty: DifficultyLevel = DifficultyLevel.MEDIUM,
    val snoozeMinutes: Int = 5,
    val sound: String = "default",
    val repeatDays: Set<DayOfWeek> = emptySet(),
)
