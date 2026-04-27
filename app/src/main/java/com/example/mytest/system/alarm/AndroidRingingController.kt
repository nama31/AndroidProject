package com.example.mytest.system.alarm

import android.content.Context
import android.content.Intent
import com.example.mytest.domain.ringing.RingingController

/**
 * `RingingController` implementation that sends `ACTION_STOP_RINGING` to
 * the foreground [AlarmRingtoneService].
 *
 * Safe to call when nothing is ringing — the service handles the action as
 * a no-op (`onStartCommand` → `stopSelf`).
 */
class AndroidRingingController(
    private val context: Context,
) : RingingController {

    override fun stop() {
        val intent = Intent(context, AlarmRingtoneService::class.java).apply {
            action = AlarmIntents.ACTION_STOP_RINGING
        }
        try {
            // startService (NOT startForegroundService) — we only want the
            // service to receive the stop signal. If it's not running this
            // is a harmless no-op.
            context.startService(intent)
        } catch (_: IllegalStateException) {
            // Background-start restriction on API 31+ when the service is
            // already gone — nothing to stop.
        }
    }
}
