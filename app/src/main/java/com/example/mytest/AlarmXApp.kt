package com.example.mytest

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.example.mytest.system.alarm.AlarmRingtoneService
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Application subclass. Bootstraps the Hilt DI graph via [HiltAndroidApp]
 * and plugs the [HiltWorkerFactory] into WorkManager so `@HiltWorker`
 * classes (like `AlarmRescheduleWorker`) can be constructor-injected.
 *
 * Also registers the alarm notification channel before any Activity is
 * created, so full-screen-intent firing works on a fresh install.
 *
 * Registered via `android:name=".AlarmXApp"` in `AndroidManifest.xml`.
 */
@HiltAndroidApp
class AlarmXApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        AlarmRingtoneService.ensureChannel(this)
    }
}
