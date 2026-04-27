package com.example.mytest.data.prefs

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

/**
 * Centralised Preferences DataStore keys.
 *
 * Keys are namespaced (`pref_*`) to avoid collisions if the same DataStore
 * instance is ever shared. Defaults live alongside each key so the
 * implementation in [PreferencesRepositoryImpl] remains a thin wrapper.
 */
internal object PreferencesKeys {

    const val DATASTORE_NAME = "user_preferences"

    val DIFFICULTY = stringPreferencesKey("pref_difficulty")
    val SNOOZE_ENABLED = booleanPreferencesKey("pref_snooze_enabled")
    val DEFAULT_SNOOZE_MINUTES = intPreferencesKey("pref_default_snooze_minutes")
    val SOUND = stringPreferencesKey("pref_sound")
    val THEME_MODE = stringPreferencesKey("pref_theme_mode")
    val MAX_SNOOZE_COUNT = intPreferencesKey("pref_max_snooze_count")
    val HAPTICS_ON_WRONG_ANSWER = booleanPreferencesKey("pref_haptics_on_wrong_answer")
}
