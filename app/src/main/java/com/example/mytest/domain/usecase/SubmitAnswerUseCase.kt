package com.example.mytest.domain.usecase

import com.example.mytest.domain.model.ArithmeticTask
import com.example.mytest.domain.repository.AlarmRepository
import javax.inject.Inject

/**
 * Compares the user's input against the active challenge's expected answer.
 *
 * On [SubmitAnswerResult.Correct] the alarm is dismissed via the repository;
 * other outcomes leave persistent state untouched (the ViewModel decides
 * whether to regenerate the challenge, increment a counter, vibrate, etc.).
 */
class SubmitAnswerUseCase @Inject constructor(
    private val alarmRepository: AlarmRepository,
) {
    suspend operator fun invoke(
        alarmId: Long?,
        challenge: ArithmeticTask?,
        answer: Int,
    ): SubmitAnswerResult {
        if (alarmId == null || challenge == null) return SubmitAnswerResult.NoActiveChallenge
        return if (answer == challenge.answer()) {
            alarmRepository.dismiss(alarmId)
            SubmitAnswerResult.Correct
        } else {
            SubmitAnswerResult.Incorrect
        }
    }
}

sealed class SubmitAnswerResult {
    /** Answer matched; the alarm has been dismissed. */
    data object Correct : SubmitAnswerResult()

    /** Answer did not match; the alarm is still ringing. */
    data object Incorrect : SubmitAnswerResult()

    /** No active challenge — typically because the alarm was already handled. */
    data object NoActiveChallenge : SubmitAnswerResult()
}
