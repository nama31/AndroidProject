package com.example.mytest.ui.alarm

/**
 * One-shot UX signal emitted by [AlarmViewModel].
 *
 * Modelled as a [kotlinx.coroutines.flow.SharedFlow] separately from
 * [AlarmUiState] because navigation, haptics, and toasts are events, not
 * state — replaying them after a config change would fire the side-effect
 * twice.
 */
sealed interface AlarmEvent {
    /** Open the full-screen Dismiss UI for the given alarm. */
    data class NavigateToDismiss(val alarmId: Long) : AlarmEvent

    /** Close the Dismiss UI and return to the alarm list. */
    data object NavigateBackToList : AlarmEvent

    /** User submitted a wrong answer — UI should buzz / shake. */
    data object WrongAnswerFeedback : AlarmEvent

    /** User submitted the correct answer. */
    data object CorrectAnswerFeedback : AlarmEvent

    /** Snooze succeeded; UI may show a confirmation. */
    data class Snoozed(val nextTriggerEpochMillis: Long) : AlarmEvent

    /** Generic error to surface as a toast / snackbar. */
    data class ShowError(val message: String) : AlarmEvent
}
