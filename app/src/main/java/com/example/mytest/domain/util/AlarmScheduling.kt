package com.example.mytest.domain.util

import com.example.mytest.domain.model.Alarm
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

/**
 * Pure-Kotlin scheduling helpers. Framework-free so they can be unit-tested
 * without a device.
 */
object AlarmScheduling {

    /**
     * Given a time-of-day (`hour`/`minute`), a (possibly empty) set of
     * repeat days, and a reference [now], compute the epoch-millis of the
     * next time that `hour:minute` occurs.
     *
     * Rules:
     * - **One-shot** (`repeatDays.isEmpty()`): today at `hour:minute` if
     *   that time is strictly in the future, otherwise tomorrow.
     * - **Repeating**: today at `hour:minute` if today's day-of-week is in
     *   `repeatDays` **and** that time is strictly in the future; otherwise
     *   the earliest day in `repeatDays` strictly after today.
     *
     * Always deterministic for a given [zone] and [now].
     */
    fun nextTriggerEpochMillis(
        hour: Int,
        minute: Int,
        repeatDays: Set<DayOfWeek>,
        zone: ZoneId = ZoneId.systemDefault(),
        now: LocalDateTime = LocalDateTime.now(zone),
    ): Long {
        val today = now.toLocalDate()
        val targetTime = LocalTime.of(hour, minute)

        val nextDate: LocalDate = if (repeatDays.isEmpty()) {
            if (targetTime.isAfter(now.toLocalTime())) today else today.plusDays(1)
        } else {
            val todayIsEligible =
                today.dayOfWeek in repeatDays && targetTime.isAfter(now.toLocalTime())
            if (todayIsEligible) {
                today
            } else {
                (1..7).asSequence()
                    .map { today.plusDays(it.toLong()) }
                    .first { it.dayOfWeek in repeatDays }
            }
        }
        return LocalDateTime.of(nextDate, targetTime)
            .atZone(zone)
            .toInstant()
            .toEpochMilli()
    }

    /**
     * Advance a past-due repeating alarm to its next occurrence **strictly
     * after** [after]. Intended for:
     *   - `AlarmRepository.dismiss(alarm)` when `repeatDays.isNotEmpty()`.
     *   - `AlarmRescheduleWorker` on boot, for alarms whose stored trigger
     *     is already in the past but whose `repeatDays` say they should
     *     keep firing.
     *
     * Returns `null` for one-shot alarms (`repeatDays.isEmpty()`) — the
     * caller decides what to do with them (typically disable).
     *
     * The returned [Alarm] is a copy of [alarm] with [Alarm.triggerAtEpochMillis]
     * replaced. [Alarm.enabled] is left untouched.
     */
    fun advanceRepeating(
        alarm: Alarm,
        after: Instant = Instant.now(),
        zone: ZoneId = ZoneId.systemDefault(),
    ): Alarm? {
        if (alarm.repeatDays.isEmpty()) return null
        val originalTime = Instant.ofEpochMilli(alarm.triggerAtEpochMillis)
            .atZone(zone)
            .toLocalTime()
        val reference = LocalDateTime.ofInstant(after, zone)
        val next = nextTriggerEpochMillis(
            hour = originalTime.hour,
            minute = originalTime.minute,
            repeatDays = alarm.repeatDays,
            zone = zone,
            now = reference,
        )
        return alarm.copy(triggerAtEpochMillis = next)
    }
}
