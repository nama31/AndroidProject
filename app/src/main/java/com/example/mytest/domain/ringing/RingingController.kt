package com.example.mytest.domain.ringing

/**
 * Domain-level contract for stopping a currently-ringing alarm.
 *
 * Implemented in the system layer (`AndroidRingingController`) on top of the
 * foreground `AlarmRingtoneService`. Keeping the interface here lets the
 * ViewModel call [stop] without depending on Android types.
 */
interface RingingController {
    /**
     * Stop any ringtone playback / WakeLock / foreground service tied to a
     * currently-firing alarm. No-op if nothing is ringing.
     */
    fun stop()
}
