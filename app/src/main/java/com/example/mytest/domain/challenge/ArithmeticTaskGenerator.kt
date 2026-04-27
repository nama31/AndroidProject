package com.example.mytest.domain.challenge

import com.example.mytest.domain.model.ArithmeticOperation
import com.example.mytest.domain.model.ArithmeticTask
import com.example.mytest.domain.model.DifficultyLevel
import kotlin.random.Random

/**
 * Default [ChallengeProvider] producing arithmetic tasks.
 *
 * Difficulty mapping:
 * - [DifficultyLevel.EASY]:   single-digit operands, addition only.
 * - [DifficultyLevel.MEDIUM]: two-digit operands (10..49), addition or
 *                             subtraction.
 * - [DifficultyLevel.HARD]:   two-digit operands (20..99), addition,
 *                             subtraction, or multiplication.
 *
 * The [random] dependency is injectable so unit tests can pass `Random(seed)`
 * for deterministic output.
 */
class ArithmeticTaskGenerator(
    private val random: Random = Random.Default,
) : ChallengeProvider {

    override fun generate(difficulty: DifficultyLevel): ArithmeticTask {
        val range = when (difficulty) {
            DifficultyLevel.EASY -> 1..9
            DifficultyLevel.MEDIUM -> 10..49
            DifficultyLevel.HARD -> 20..99
        }
        val operation = when (difficulty) {
            DifficultyLevel.EASY -> ArithmeticOperation.ADD
            DifficultyLevel.MEDIUM ->
                if (random.nextBoolean()) ArithmeticOperation.ADD
                else ArithmeticOperation.SUBTRACT
            DifficultyLevel.HARD -> HARD_OPERATIONS[random.nextInt(HARD_OPERATIONS.size)]
        }

        return ArithmeticTask(
            left = random.nextInt(range.first, range.last + 1),
            right = random.nextInt(range.first, range.last + 1),
            operation = operation,
        )
    }

    private companion object {
        val HARD_OPERATIONS = arrayOf(
            ArithmeticOperation.ADD,
            ArithmeticOperation.SUBTRACT,
            ArithmeticOperation.MULTIPLY,
        )
    }
}
