package com.example.mytest.domain.scheduler

import com.example.mytest.domain.model.Alarm

/**
 * Domain-level contract for arming/cancelling system alarms.
 *
 * Implemented in the system layer (out of scope for this PR) on top of
 * `AlarmManager.setAlarmClock` / `setExactAndAllowWhileIdle`. Keeping the
 * interface here lets repository code, ViewModels and unit tests stay free of
 * Android framework imports.
 */
interface AlarmScheduler {

    /**
     * Arm [alarm] in the OS so that it fires at [Alarm.triggerAtEpochMillis].
     * If [alarm] is already scheduled, the implementation replaces the
     * existing schedule.
     */
    fun schedule(alarm: Alarm)

    /** Cancel any pending OS-level alarm with [alarmId], if one exists. */
    fun cancel(alarmId: Long)
}
