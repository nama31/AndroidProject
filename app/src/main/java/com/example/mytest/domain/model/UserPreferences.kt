package com.example.mytest.domain.model

/**
 * Global user preferences applied to newly-created alarms and to the alarm
 * runtime (snooze/haptics behaviour).
 *
 * Persisted via Preferences DataStore — see
 * [com.example.mytest.data.prefs.PreferencesRepositoryImpl].
 */
data class UserPreferences(
    val difficulty: DifficultyLevel = DifficultyLevel.MEDIUM,
    val snoozeEnabled: Boolean = true,
    val defaultSnoozeMinutes: Int = 5,
    val sound: String = "default",
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val maxSnoozeCount: Int = 3,
    val hapticsOnWrongAnswer: Boolean = true,
)

/** UI theme override; `SYSTEM` follows the device-level setting. */
enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
}
