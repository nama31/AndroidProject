package com.example.mytest.domain.challenge

import com.example.mytest.domain.model.ArithmeticOperation
import com.example.mytest.domain.model.ArithmeticTask
import com.example.mytest.domain.model.DifficultyLevel
import kotlin.random.Random

/**
 * Default [ChallengeProvider] producing arithmetic tasks.
 *
 * Rules enforced for every difficulty:
 * - Result is **non-negative** (subtraction always swaps operands so left ≥
 *   right).
 * - Result fits in **at most 4 digits** so it's reasonable to enter on a
 *   numeric keypad.
 *
 * Difficulty mapping:
 * | Difficulty | Operations    | Operands             | Max result |
 * |------------|---------------|----------------------|------------|
 * | EASY       | ADD           | 1..9                 |     18     |
 * | MEDIUM     | ADD, SUBTRACT | 10..49               |     98     |
 * | HARD       | ADD, SUBTRACT | 20..99 (for ±)       |    198     |
 * | HARD       | MULTIPLY      | 2..12 (multiplication|    144     |
 * |            |               | tables — short input)|            |
 *
 * The [random] dependency is injectable so unit tests can pass `Random(seed)`
 * for deterministic output.
 */
class ArithmeticTaskGenerator(
    private val random: Random = Random.Default,
) : ChallengeProvider {

    override fun generate(difficulty: DifficultyLevel): ArithmeticTask {
        val operation = pickOperation(difficulty)
        val (left, right) = pickOperands(difficulty, operation)
        return ArithmeticTask(left = left, right = right, operation = operation)
    }

    private fun pickOperation(difficulty: DifficultyLevel): ArithmeticOperation =
        when (difficulty) {
            DifficultyLevel.EASY -> ArithmeticOperation.ADD
            DifficultyLevel.MEDIUM -> MEDIUM_OPERATIONS.random()
            DifficultyLevel.HARD -> HARD_OPERATIONS.random()
        }

    private fun pickOperands(
        difficulty: DifficultyLevel,
        operation: ArithmeticOperation,
    ): Pair<Int, Int> {
        val range = when (operation) {
            ArithmeticOperation.MULTIPLY -> MULTIPLY_RANGE
            else -> when (difficulty) {
                DifficultyLevel.EASY -> EASY_RANGE
                DifficultyLevel.MEDIUM -> MEDIUM_RANGE
                DifficultyLevel.HARD -> HARD_RANGE
            }
        }

        val a = randomInRange(range)
        val b = randomInRange(range)

        return when (operation) {
            // Ensure subtraction is non-negative — order operands so the
            // larger one comes first.
            ArithmeticOperation.SUBTRACT -> if (a >= b) a to b else b to a
            else -> a to b
        }
    }

    private fun randomInRange(range: IntRange): Int =
        random.nextInt(range.first, range.last + 1)

    private fun <T> Array<T>.random(): T = this[random.nextInt(size)]

    private companion object {
        val EASY_RANGE = 1..9
        val MEDIUM_RANGE = 10..49
        val HARD_RANGE = 20..99
        // Times-tables — keeps multiplication results <= 144 so the answer
        // is at most 3 digits.
        val MULTIPLY_RANGE = 2..12

        val MEDIUM_OPERATIONS = arrayOf(
            ArithmeticOperation.ADD,
            ArithmeticOperation.SUBTRACT,
        )
        val HARD_OPERATIONS = arrayOf(
            ArithmeticOperation.ADD,
            ArithmeticOperation.SUBTRACT,
            ArithmeticOperation.MULTIPLY,
        )
    }
}
