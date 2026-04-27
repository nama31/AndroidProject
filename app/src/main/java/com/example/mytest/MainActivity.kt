package com.example.mytest

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.example.mytest.system.alarm.AlarmIntents
import com.example.mytest.ui.nav.AlarmXNavGraph
import com.example.mytest.ui.theme.AlarmXTheme

/**
 * Single Activity for AlarmX.
 *
 * Two ways to launch:
 *   1. Normal app launch — start at the alarm list.
 *   2. From the alarm full-screen intent (lock-screen) — start straight on
 *      the dismiss screen for `EXTRA_ALARM_ID`.
 *
 * The activity opts in to `setShowWhenLocked` / `setTurnScreenOn` so the
 * dismiss UI appears over the keyguard the moment the alarm fires.
 */
class MainActivity : ComponentActivity() {

    private var initialAlarmId by mutableStateOf<Long?>(null)

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { /* user choice persists in OS — no-op here */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configureLockScreen()
        enableEdgeToEdge()
        initialAlarmId = readAlarmIdFromIntent(intent)

        setContent {
            AlarmXTheme {
                AlarmXNavGraph(initialAlarmId = initialAlarmId)
            }
        }

        maybeRequestNotificationPermission()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        // Re-deliver alarm id when the activity is brought back to the front
        // (e.g. user dismissed the activity but a new alarm fires).
        readAlarmIdFromIntent(intent)?.let { initialAlarmId = it }
    }

    private fun configureLockScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    android.view.WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            )
        }
    }

    private fun readAlarmIdFromIntent(intent: Intent?): Long? {
        if (intent == null) return null
        if (intent.action != AlarmIntents.ACTION_SHOW_DISMISS) return null
        val id = intent.getLongExtra(AlarmIntents.EXTRA_ALARM_ID, -1L)
        return id.takeIf { it != -1L }
    }

    private fun maybeRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val granted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
