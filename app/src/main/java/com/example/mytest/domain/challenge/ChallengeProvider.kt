package com.example.mytest.domain.challenge

import com.example.mytest.domain.model.ArithmeticTask
import com.example.mytest.domain.model.DifficultyLevel

/**
 * Generates a single dismissal challenge for a given [DifficultyLevel].
 *
 * Currently returns [ArithmeticTask]s. The architecture roadmap (§11) extends
 * this to a sealed `Challenge` hierarchy (memory / sequence / barcode / shake)
 * — when that lands, this interface becomes `ChallengeProvider<Challenge>`.
 * For now we keep it concrete to avoid premature generality.
 */
fun interface ChallengeProvider {
    fun generate(difficulty: DifficultyLevel): ArithmeticTask
}
