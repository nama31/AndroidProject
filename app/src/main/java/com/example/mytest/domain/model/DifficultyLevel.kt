package com.example.mytest.domain.model

/**
 * Difficulty of the cognitive dismissal challenge.
 *
 * Drives both the numeric range and the set of operations exposed by
 * [com.example.mytest.domain.challenge.ChallengeProvider] (see §4.1 of
 * `alarmx-architecture.md`).
 */
enum class DifficultyLevel {
    EASY,
    MEDIUM,
    HARD,
}
