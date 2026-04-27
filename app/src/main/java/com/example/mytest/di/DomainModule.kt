package com.example.mytest.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlin.random.Random

/**
 * Domain-layer providers for values that Hilt otherwise can't inject
 * (built-in Kotlin types, etc.).
 *
 * `Random` is surfaced as a singleton so unit tests can replace it with a
 * seeded instance through a test module.
 */
@Module
@InstallIn(SingletonComponent::class)
object DomainModule {

    @Provides
    fun provideRandom(): Random = Random.Default
}
