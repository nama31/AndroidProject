package com.example.mytest.ui.alarm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mytest.domain.challenge.ChallengeProvider
import com.example.mytest.domain.repository.AlarmRepository
import com.example.mytest.domain.repository.PreferencesRepository
import com.example.mytest.domain.usecase.CreateAlarmUseCase
import com.example.mytest.domain.usecase.SnoozeUseCase
import com.example.mytest.domain.usecase.SubmitAnswerResult
import com.example.mytest.domain.usecase.SubmitAnswerUseCase
import com.example.mytest.domain.usecase.TriggerAlarmUseCase
import java.time.DayOfWeek
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Single ViewModel that owns the user-facing alarm flow: list, create,
 * dismiss, snooze, and live preferences.
 *
 * The architecture splits per-screen ViewModels (alarmlist / create /
 * dismiss / preferences) — at this stage we expose one shared instance so
 * all screens can read the same [AlarmUiState] without prop-drilling. When
 * the screens land we can split the state into per-screen slices behind the
 * same use cases.
 *
 * Constructor-injectable; Hilt annotations will be added when the DI graph
 * is wired up.
 */
class AlarmViewModel(
    private val alarmRepository: AlarmRepository,
    private val preferencesRepository: PreferencesRepository,
    private val challengeProvider: ChallengeProvider,
    private val createAlarmUseCase: CreateAlarmUseCase,
    private val triggerAlarmUseCase: TriggerAlarmUseCase,
    private val submitAnswerUseCase: SubmitAnswerUseCase,
    private val snoozeUseCase: SnoozeUseCase,
    private val now: () -> Long = { System.currentTimeMillis() },
) : ViewModel() {

    private val _state = MutableStateFlow(AlarmUiState())
    val state: StateFlow<AlarmUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<AlarmEvent>(extraBufferCapacity = 8)
    val events: SharedFlow<AlarmEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            alarmRepository.observeAlarms().collect { alarms ->
                _state.update { it.copy(alarms = alarms) }
            }
        }
        viewModelScope.launch {
            preferencesRepository.preferences.collect { prefs ->
                _state.update { it.copy(preferences = prefs) }
            }
        }
    }

    // ------------------------------------------------------------------
    // List screen
    // ------------------------------------------------------------------

    fun createAlarm(
        id: Long,
        triggerAtEpochMillis: Long,
        label: String = "",
        repeatDays: Set<DayOfWeek> = emptySet(),
    ) {
        launchSafely {
            createAlarmUseCase(
                id = id,
                triggerAtEpochMillis = triggerAtEpochMillis,
                label = label,
                repeatDays = repeatDays,
            )
        }
    }

    fun toggleAlarm(alarmId: Long, enabled: Boolean) {
        launchSafely { alarmRepository.setEnabled(alarmId, enabled) }
    }

    fun deleteAlarm(alarmId: Long) {
        launchSafely { alarmRepository.delete(alarmId) }
    }

    // ------------------------------------------------------------------
    // Dismiss screen
    // ------------------------------------------------------------------

    /** Called by the receiver/service when the OS fires an alarm. */
    fun onAlarmTriggered(alarmId: Long) {
        launchSafely {
            val trigger = triggerAlarmUseCase(alarmId)
            if (trigger == null) {
                _events.emit(AlarmEvent.NavigateBackToList)
                return@launchSafely
            }
            _state.update {
                it.copy(
                    activeAlarmId = trigger.alarmId,
                    activeChallenge = trigger.challenge,
                    fullScreenVisible = true,
                    incorrectAttempts = 0,
                )
            }
            _events.emit(AlarmEvent.NavigateToDismiss(trigger.alarmId))
        }
    }

    fun submitAnswer(answer: Int) {
        val s = _state.value
        launchSafely {
            when (submitAnswerUseCase(s.activeAlarmId, s.activeChallenge, answer)) {
                SubmitAnswerResult.Correct -> {
                    _state.update {
                        it.copy(
                            activeAlarmId = null,
                            activeChallenge = null,
                            fullScreenVisible = false,
                            incorrectAttempts = 0,
                        )
                    }
                    _events.emit(AlarmEvent.CorrectAnswerFeedback)
                    _events.emit(AlarmEvent.NavigateBackToList)
                }
                SubmitAnswerResult.Incorrect -> {
                    val difficulty = s.preferences.difficulty
                    _state.update {
                        it.copy(
                            // Regenerate so users can't memorise a single answer.
                            activeChallenge = challengeProvider.generate(difficulty),
                            incorrectAttempts = it.incorrectAttempts + 1,
                        )
                    }
                    _events.emit(AlarmEvent.WrongAnswerFeedback)
                }
                SubmitAnswerResult.NoActiveChallenge -> {
                    _state.update {
                        it.copy(
                            activeAlarmId = null,
                            activeChallenge = null,
                            fullScreenVisible = false,
                            incorrectAttempts = 0,
                        )
                    }
                    _events.emit(AlarmEvent.NavigateBackToList)
                }
            }
        }
    }

    fun snoozeActiveAlarm() {
        val activeId = _state.value.activeAlarmId ?: return
        launchSafely {
            val snoozed = snoozeUseCase(activeId, now())
            if (snoozed == null) {
                _events.emit(AlarmEvent.ShowError("Snooze is disabled or alarm is gone"))
                return@launchSafely
            }
            _state.update {
                it.copy(
                    activeAlarmId = null,
                    activeChallenge = null,
                    fullScreenVisible = false,
                    incorrectAttempts = 0,
                )
            }
            _events.emit(AlarmEvent.Snoozed(snoozed.triggerAtEpochMillis))
            _events.emit(AlarmEvent.NavigateBackToList)
        }
    }

    fun dismissError() {
        _state.update { it.copy(error = null) }
    }

    // ------------------------------------------------------------------
    // Internals
    // ------------------------------------------------------------------

    private fun launchSafely(block: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(loading = true, error = null) }
                block()
            } catch (cancelled: kotlinx.coroutines.CancellationException) {
                throw cancelled
            } catch (t: Throwable) {
                val message = t.message ?: "Unknown error"
                _state.update { it.copy(error = message) }
                _events.emit(AlarmEvent.ShowError(message))
            } finally {
                _state.update { it.copy(loading = false) }
            }
        }
    }
}
