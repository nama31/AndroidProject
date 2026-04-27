package com.example.mytest.domain.repository

import com.example.mytest.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow

/**
 * Domain-level contract for the user-preferences store.
 *
 * Implementations are backed by Preferences DataStore — see
 * [com.example.mytest.data.prefs.PreferencesRepositoryImpl].
 */
interface PreferencesRepository {

    /** Reactive stream of the current preferences. Always emits a value. */
    val preferences: Flow<UserPreferences>

    /** One-shot snapshot of the current preferences. */
    suspend fun get(): UserPreferences

    /**
     * Atomically transform the current preferences and persist the result.
     * The lambda runs on the DataStore writer thread; do not perform blocking
     * IO inside it.
     */
    suspend fun update(transform: (UserPreferences) -> UserPreferences)
}
