package com.example.mytest.domain.model

/** Operation rendered in an [ArithmeticTask] prompt. */
enum class ArithmeticOperation {
    ADD,
    SUBTRACT,
    MULTIPLY,
}

/**
 * Single arithmetic dismissal challenge.
 *
 * Domain-only: the prompt string is derived deterministically from
 * ([left], [right], [operation]) so this class can be unit-tested without
 * any Compose / Android dependencies.
 */
data class ArithmeticTask(
    val left: Int,
    val right: Int,
    val operation: ArithmeticOperation,
) {
    /** Human-readable prompt, e.g. `"47 + 28"`. */
    val prompt: String
        get() = when (operation) {
            ArithmeticOperation.ADD -> "$left + $right"
            ArithmeticOperation.SUBTRACT -> "$left - $right"
            ArithmeticOperation.MULTIPLY -> "$left × $right"
        }

    /** The expected integer answer. */
    fun answer(): Int = when (operation) {
        ArithmeticOperation.ADD -> left + right
        ArithmeticOperation.SUBTRACT -> left - right
        ArithmeticOperation.MULTIPLY -> left * right
    }
}

/**
 * Snapshot delivered by the system layer to the ViewModel when an alarm
 * fires: the alarm being triggered plus a freshly-generated challenge.
 */
data class AlarmTrigger(
    val alarmId: Long,
    val challenge: ArithmeticTask,
)
