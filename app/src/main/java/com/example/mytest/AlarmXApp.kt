package com.example.mytest

import android.app.Application
import com.example.mytest.system.alarm.AlarmRingtoneService

/**
 * Application subclass — initialises the global service-locator
 * [AppGraph] and registers the alarm notification channel before any
 * Activity is created.
 *
 * Registered via `android:name=".AlarmXApp"` in `AndroidManifest.xml`.
 */
class AlarmXApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppGraph.init(this)
        AlarmRingtoneService.ensureChannel(this)
    }
}
