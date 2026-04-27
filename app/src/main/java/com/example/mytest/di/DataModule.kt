package com.example.mytest.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.example.mytest.data.db.AlarmDao
import com.example.mytest.data.db.AppDatabase
import com.example.mytest.data.prefs.PreferencesKeys
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Provides the data-layer singletons: Room [AppDatabase], its DAO, and the
 * Preferences [DataStore]. Everything here is `@Singleton` — one instance
 * per process.
 */
@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    /**
     * Top-level delegated property that owns the DataStore file on disk.
     * Kept at object scope so the file is created exactly once; Hilt just
     * surfaces the underlying [DataStore] as an injectable singleton.
     */
    private val Context.userPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
        name = PreferencesKeys.DATASTORE_NAME,
    )

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME,
        ).build()

    @Provides
    fun provideAlarmDao(database: AppDatabase): AlarmDao = database.alarmDao()

    @Provides
    @Singleton
    fun provideUserPreferencesDataStore(
        @ApplicationContext context: Context,
    ): DataStore<Preferences> = context.userPreferencesDataStore
}
