package com.example.mytest

import android.app.Application

/**
 * Application subclass — initialises the global service-locator
 * [AppGraph] before any Activity is created.
 *
 * Registered via `android:name=".AlarmXApp"` in `AndroidManifest.xml`.
 */
class AlarmXApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppGraph.init(this)
    }
}
