package com.example.mytest.domain.usecase

import com.example.mytest.domain.model.Alarm
import com.example.mytest.domain.repository.AlarmRepository
import com.example.mytest.domain.repository.PreferencesRepository
import java.time.DayOfWeek
import javax.inject.Inject

/**
 * Persists a new alarm using the user's current preferences for fields the
 * caller doesn't override (difficulty, snooze length, sound).
 *
 * The caller supplies [id] explicitly — see §4.1 of `alarmx-architecture.md`
 * for why ids are caller-provided (used as the `AlarmManager` request code).
 */
class CreateAlarmUseCase @Inject constructor(
    private val alarmRepository: AlarmRepository,
    private val preferencesRepository: PreferencesRepository,
) {
    suspend operator fun invoke(
        id: Long,
        triggerAtEpochMillis: Long,
        label: String = "",
        repeatDays: Set<DayOfWeek> = emptySet(),
    ): Alarm {
        val prefs = preferencesRepository.get()
        val alarm = Alarm(
            id = id,
            triggerAtEpochMillis = triggerAtEpochMillis,
            label = label,
            enabled = true,
            difficulty = prefs.difficulty,
            snoozeMinutes = prefs.defaultSnoozeMinutes,
            sound = prefs.sound,
            repeatDays = repeatDays,
        )
        alarmRepository.save(alarm)
        return alarm
    }
}
