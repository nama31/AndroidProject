package com.example.mytest.ui.alarm

import com.example.mytest.domain.model.Alarm
import com.example.mytest.domain.model.ArithmeticTask
import com.example.mytest.domain.model.UserPreferences

/**
 * UI state surface for [AlarmViewModel].
 *
 * Mirrors §4.3 of `alarmx-architecture.md` and adds [preferences] +
 * [loading] / [error] flags so all screens consuming this VM can render
 * without juggling additional flows.
 */
data class AlarmUiState(
    val alarms: List<Alarm> = emptyList(),
    val preferences: UserPreferences = UserPreferences(),
    /** id of the alarm currently being challenged, or `null` when idle. */
    val activeAlarmId: Long? = null,
    /** challenge for [activeAlarmId], regenerated on every wrong answer. */
    val activeChallenge: ArithmeticTask? = null,
    /** controls the full-screen Dismiss UI on the navigation layer. */
    val fullScreenVisible: Boolean = false,
    /** wrong-answer counter for the current challenge session. */
    val incorrectAttempts: Int = 0,
    val loading: Boolean = false,
    val error: String? = null,
)
