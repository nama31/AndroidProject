package com.example.mytest.domain.usecase

import com.example.mytest.domain.challenge.ChallengeProvider
import com.example.mytest.domain.model.AlarmTrigger
import com.example.mytest.domain.repository.AlarmRepository
import javax.inject.Inject

/**
 * Produces an [AlarmTrigger] (alarm + freshly-generated challenge) for the
 * given id. Returns `null` if the alarm doesn't exist or is currently
 * disabled — callers should treat that as a no-op (e.g. dismiss the
 * full-screen UI).
 *
 * Pure read-side use case: it does NOT mark the alarm as fired or modify the
 * repository. Dismissal is the job of [SubmitAnswerUseCase].
 */
class TriggerAlarmUseCase @Inject constructor(
    private val alarmRepository: AlarmRepository,
    private val challengeProvider: ChallengeProvider,
) {
    suspend operator fun invoke(alarmId: Long): AlarmTrigger? {
        val alarm = alarmRepository.findById(alarmId) ?: return null
        if (!alarm.enabled) return null
        return AlarmTrigger(
            alarmId = alarm.id,
            challenge = challengeProvider.generate(alarm.difficulty),
        )
    }
}
