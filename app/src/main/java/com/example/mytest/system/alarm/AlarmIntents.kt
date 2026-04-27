package com.example.mytest.system.alarm

/**
 * String constants shared between [AlarmManagerScheduler],
 * [AlarmBroadcastReceiver], [AlarmRingtoneService] and `MainActivity`.
 *
 * Keeping all action names + extras in one place avoids typos sneaking
 * between the receiver and the service.
 */
object AlarmIntents {

    /** Action used by [AlarmBroadcastReceiver] to receive PendingIntents from `AlarmManager`. */
    const val ACTION_ALARM_FIRED = "com.example.mytest.action.ALARM_FIRED"

    /** Sent to [AlarmRingtoneService] to stop ringing and tear down. */
    const val ACTION_STOP_RINGING = "com.example.mytest.action.STOP_RINGING"

    /** Used on `MainActivity` to force navigation straight to the dismiss screen. */
    const val ACTION_SHOW_DISMISS = "com.example.mytest.action.SHOW_DISMISS"

    /** Long extra: the alarm id the intent applies to. */
    const val EXTRA_ALARM_ID = "com.example.mytest.extra.ALARM_ID"

    /** Notification channel id used by the foreground ringtone service. */
    const val CHANNEL_ID_ALARM = "alarmx.alarm"

    /** Stable notification id for the foreground ringtone service. */
    const val NOTIFICATION_ID_ALARM = 0xA1A2
}
