package com.example.mytest.di

import com.example.mytest.data.prefs.PreferencesRepositoryImpl
import com.example.mytest.data.repository.DefaultAlarmRepository
import com.example.mytest.domain.challenge.ArithmeticTaskGenerator
import com.example.mytest.domain.challenge.ChallengeProvider
import com.example.mytest.domain.repository.AlarmRepository
import com.example.mytest.domain.repository.PreferencesRepository
import com.example.mytest.domain.ringing.RingingController
import com.example.mytest.domain.scheduler.AlarmScheduler
import com.example.mytest.system.alarm.AlarmManagerScheduler
import com.example.mytest.system.alarm.AndroidRingingController
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Binds domain interfaces to their production implementations. Every
 * binding is a singleton: the repositories, scheduler, and ringing
 * controller hold no per-request state.
 *
 * Use-cases are not listed here — they use `@Inject constructor` directly,
 * so Hilt can synthesize their providers.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAlarmRepository(impl: DefaultAlarmRepository): AlarmRepository

    @Binds
    @Singleton
    abstract fun bindPreferencesRepository(impl: PreferencesRepositoryImpl): PreferencesRepository

    @Binds
    @Singleton
    abstract fun bindAlarmScheduler(impl: AlarmManagerScheduler): AlarmScheduler

    @Binds
    @Singleton
    abstract fun bindRingingController(impl: AndroidRingingController): RingingController

    @Binds
    @Singleton
    abstract fun bindChallengeProvider(impl: ArithmeticTaskGenerator): ChallengeProvider
}
