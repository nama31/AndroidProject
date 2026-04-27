package com.example.mytest.data.prefs

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import com.example.mytest.domain.model.DifficultyLevel
import com.example.mytest.domain.model.ThemeMode
import com.example.mytest.domain.model.UserPreferences
import com.example.mytest.domain.repository.PreferencesRepository
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Preferences DataStore-backed implementation of [PreferencesRepository].
 *
 * Choice rationale: the [UserPreferences] schema is flat (booleans / ints /
 * enum names), so plain Preferences DataStore is enough. Switching to Proto
 * DataStore would buy us a strongly-typed schema, but at the cost of pulling
 * in the protobuf Gradle plugin and a generated source set — not worth it
 * here. If the schema later grows nested structures, migrate.
 *
 * Read errors (`IOException`) are surfaced as empty [Preferences] so callers
 * always observe at least the in-memory defaults rather than crashing.
 */
class PreferencesRepositoryImpl(
    private val dataStore: DataStore<Preferences>,
    private val defaults: UserPreferences = UserPreferences(),
) : PreferencesRepository {

    override val preferences: Flow<UserPreferences> = dataStore.data
        .catch { cause ->
            if (cause is IOException) emit(emptyPreferences()) else throw cause
        }
        .map { prefs -> prefs.toDomain(defaults) }

    override suspend fun get(): UserPreferences = preferences.first()

    override suspend fun update(transform: (UserPreferences) -> UserPreferences) {
        dataStore.edit { mutable ->
            val current = mutable.toDomain(defaults)
            val updated = transform(current)
            mutable.applyDomain(updated)
        }
    }
}

// ---------------------------------------------------------------------------
// Preferences <-> UserPreferences mapping
// ---------------------------------------------------------------------------

private fun Preferences.toDomain(defaults: UserPreferences): UserPreferences =
    UserPreferences(
        difficulty = this[PreferencesKeys.DIFFICULTY]
            ?.let(::parseDifficulty)
            ?: defaults.difficulty,
        snoozeEnabled = this[PreferencesKeys.SNOOZE_ENABLED]
            ?: defaults.snoozeEnabled,
        defaultSnoozeMinutes = this[PreferencesKeys.DEFAULT_SNOOZE_MINUTES]
            ?: defaults.defaultSnoozeMinutes,
        sound = this[PreferencesKeys.SOUND]
            ?: defaults.sound,
        themeMode = this[PreferencesKeys.THEME_MODE]
            ?.let(::parseThemeMode)
            ?: defaults.themeMode,
        maxSnoozeCount = this[PreferencesKeys.MAX_SNOOZE_COUNT]
            ?: defaults.maxSnoozeCount,
        hapticsOnWrongAnswer = this[PreferencesKeys.HAPTICS_ON_WRONG_ANSWER]
            ?: defaults.hapticsOnWrongAnswer,
    )

private fun androidx.datastore.preferences.core.MutablePreferences.applyDomain(
    prefs: UserPreferences,
) {
    this[PreferencesKeys.DIFFICULTY] = prefs.difficulty.name
    this[PreferencesKeys.SNOOZE_ENABLED] = prefs.snoozeEnabled
    this[PreferencesKeys.DEFAULT_SNOOZE_MINUTES] = prefs.defaultSnoozeMinutes
    this[PreferencesKeys.SOUND] = prefs.sound
    this[PreferencesKeys.THEME_MODE] = prefs.themeMode.name
    this[PreferencesKeys.MAX_SNOOZE_COUNT] = prefs.maxSnoozeCount
    this[PreferencesKeys.HAPTICS_ON_WRONG_ANSWER] = prefs.hapticsOnWrongAnswer
}

private fun parseDifficulty(raw: String): DifficultyLevel =
    runCatching { DifficultyLevel.valueOf(raw) }
        .getOrDefault(DifficultyLevel.MEDIUM)

private fun parseThemeMode(raw: String): ThemeMode =
    runCatching { ThemeMode.valueOf(raw) }
        .getOrDefault(ThemeMode.SYSTEM)
