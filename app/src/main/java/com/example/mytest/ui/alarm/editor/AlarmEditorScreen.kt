package com.example.mytest.ui.alarm.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mytest.domain.model.Alarm
import com.example.mytest.domain.model.DifficultyLevel
import com.example.mytest.ui.alarm.AlarmViewModel
import com.example.mytest.ui.common.AxChip
import com.example.mytest.ui.common.AxPrimaryButton
import com.example.mytest.ui.common.AxSecondaryButton
import com.example.mytest.ui.common.AxSegmented
import com.example.mytest.ui.common.AxToggle
import com.example.mytest.ui.theme.AlarmXTheme
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

/**
 * Unified create/edit screen. [alarmId] is `null` for create and non-null
 * when editing an existing alarm — in edit mode the screen loads the alarm
 * once and pre-populates every field.
 *
 * The screen owns local editor state (time, label, repeat days, difficulty,
 * snooze) and only commits to the ViewModel on Save, keeping changes
 * reversible until the user confirms.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmEditorScreen(
    viewModel: AlarmViewModel,
    alarmId: Long?,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = AlarmXTheme.colors
    val typography = AlarmXTheme.typography

    val isEdit = alarmId != null

    // Local editor state — defaults seeded from current preferences. When
    // we're in edit mode the LaunchedEffect below overrides them with the
    // persisted alarm values.
    var initialised by remember { mutableStateOf(!isEdit) }
    var label by remember { mutableStateOf("") }
    var repeatDays by remember { mutableStateOf<Set<DayOfWeek>>(emptySet()) }
    var difficulty by remember { mutableStateOf(state.preferences.difficulty) }
    var snoozeEnabled by remember { mutableStateOf(state.preferences.snoozeEnabled) }
    var snoozeMinutes by remember { mutableStateOf(state.preferences.defaultSnoozeMinutes) }
    var sound by remember { mutableStateOf(state.preferences.sound) }
    var existingId by remember { mutableStateOf<Long?>(null) }
    var enabled by remember { mutableStateOf(true) }

    // Default time = current local time rounded up to next hour. This is a
    // reasonable starting point — users almost always adjust it anyway.
    val defaultTime = remember {
        val now = LocalTime.now()
        LocalTime.of((now.hour + 1) % 24, 0)
    }
    val timePickerState = rememberTimePickerState(
        initialHour = defaultTime.hour,
        initialMinute = defaultTime.minute,
        is24Hour = true,
    )

    LaunchedEffect(alarmId) {
        if (alarmId == null) {
            initialised = true
            return@LaunchedEffect
        }
        val loaded = viewModel.loadAlarm(alarmId)
        if (loaded == null) {
            // Alarm was deleted out from under us — bounce back to list.
            onClose()
            return@LaunchedEffect
        }
        existingId = loaded.id
        label = loaded.label
        repeatDays = loaded.repeatDays
        difficulty = loaded.difficulty
        snoozeMinutes = loaded.snoozeMinutes
        snoozeEnabled = loaded.snoozeMinutes > 0
        sound = loaded.sound
        enabled = loaded.enabled
        val local = java.time.Instant.ofEpochMilli(loaded.triggerAtEpochMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
        timePickerState.hour = local.hour
        timePickerState.minute = local.minute
        initialised = true
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.surfaceBase,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEdit) "Edit alarm" else "New alarm",
                        style = typography.titleLg,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        // Avoid pulling in material-icons-extended: use a plain ✕.
                        Text(text = "✕", style = typography.titleLg, color = colors.textPrimary)
                    }
                },
                actions = {
                    TextButton(
                        enabled = initialised,
                        onClick = {
                            val alarm = buildAlarm(
                                existingId = existingId,
                                hour = timePickerState.hour,
                                minute = timePickerState.minute,
                                label = label,
                                repeatDays = repeatDays,
                                difficulty = difficulty,
                                snoozeMinutes = if (snoozeEnabled) snoozeMinutes else 0,
                                sound = sound,
                                enabled = enabled,
                            )
                            viewModel.saveAlarm(alarm)
                            onClose()
                        },
                    ) {
                        Text(
                            text = "Save",
                            style = typography.titleMd,
                            color = colors.accentBlue,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.surfaceBase,
                    titleContentColor = colors.textPrimary,
                ),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            // --- Time ------------------------------------------------------
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                TimePicker(
                    state = timePickerState,
                    colors = TimePickerDefaults.colors(
                        clockDialColor = colors.surfaceRaised,
                        clockDialSelectedContentColor = colors.surfaceBase,
                        clockDialUnselectedContentColor = colors.textPrimary,
                        selectorColor = colors.accentBlue,
                        containerColor = colors.surfaceBase,
                        periodSelectorBorderColor = colors.surfaceStroke,
                        periodSelectorSelectedContainerColor = colors.accentBlueSoft,
                        periodSelectorUnselectedContainerColor = colors.surfaceBase,
                        periodSelectorSelectedContentColor = colors.accentBlue,
                        periodSelectorUnselectedContentColor = colors.textSecondary,
                        timeSelectorSelectedContainerColor = colors.accentBlueSoft,
                        timeSelectorUnselectedContainerColor = colors.surfaceRaised,
                        timeSelectorSelectedContentColor = colors.accentBlue,
                        timeSelectorUnselectedContentColor = colors.textPrimary,
                    ),
                )
            }

            // --- Label -----------------------------------------------------
            Section(title = "Label") {
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = "Alarm",
                            style = typography.bodyMd,
                            color = colors.textTertiary,
                        )
                    },
                    singleLine = true,
                    textStyle = typography.bodyMd.copy(color = colors.textPrimary),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.accentBlue,
                        unfocusedBorderColor = colors.surfaceStroke,
                        focusedContainerColor = colors.surfaceBase,
                        unfocusedContainerColor = colors.surfaceBase,
                        cursorColor = colors.accentBlue,
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary,
                    ),
                )
            }

            // --- Repeat days ----------------------------------------------
            Section(title = "Repeat") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    WeekOrder.forEach { day ->
                        AxChip(
                            label = shortDay(day),
                            selected = day in repeatDays,
                            onClick = {
                                repeatDays = if (day in repeatDays) {
                                    repeatDays - day
                                } else {
                                    repeatDays + day
                                }
                            },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                Text(
                    text = if (repeatDays.isEmpty()) "One-time alarm" else "Repeats weekly",
                    style = typography.labelSm,
                    color = colors.textTertiary,
                )
            }

            // --- Difficulty -----------------------------------------------
            Section(title = "Difficulty") {
                AxSegmented(
                    options = DifficultyLevel.values().toList(),
                    selected = difficulty,
                    onSelect = { difficulty = it },
                    label = { it.name.lowercase().replaceFirstChar { c -> c.titlecase() } },
                )
            }

            // --- Snooze ----------------------------------------------------
            Section(title = "Snooze") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Allow snooze",
                            style = typography.bodyMd,
                            color = colors.textPrimary,
                        )
                        Text(
                            text = if (snoozeEnabled) "$snoozeMinutes minutes" else "Off",
                            style = typography.labelSm,
                            color = colors.textSecondary,
                        )
                    }
                    AxToggle(
                        checked = snoozeEnabled,
                        onCheckedChange = { snoozeEnabled = it },
                    )
                }
                if (snoozeEnabled) {
                    AxSegmented(
                        options = SnoozeChoices,
                        selected = snoozeMinutes.takeIf { it in SnoozeChoices } ?: 5,
                        onSelect = { snoozeMinutes = it },
                        label = { "$it m" },
                    )
                }
            }

            if (isEdit && existingId != null) {
                // Hairline before destructive action.
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(colors.surfaceStroke),
                )
                AxSecondaryButton(
                    text = "Delete alarm",
                    onClick = {
                        viewModel.deleteAlarm(existingId!!)
                        onClose()
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            AxPrimaryButton(
                text = if (isEdit) "Save changes" else "Create alarm",
                onClick = {
                    val alarm = buildAlarm(
                        existingId = existingId,
                        hour = timePickerState.hour,
                        minute = timePickerState.minute,
                        label = label,
                        repeatDays = repeatDays,
                        difficulty = difficulty,
                        snoozeMinutes = if (snoozeEnabled) snoozeMinutes else 0,
                        sound = sound,
                        enabled = enabled,
                    )
                    viewModel.saveAlarm(alarm)
                    onClose()
                },
                enabled = initialised,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun Section(
    title: String,
    content: @Composable () -> Unit,
) {
    val colors = AlarmXTheme.colors
    val typography = AlarmXTheme.typography
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = title.uppercase(),
            style = typography.labelSm,
            color = colors.textSecondary,
        )
        content()
    }
}

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

private val WeekOrder = listOf(
    DayOfWeek.MONDAY,
    DayOfWeek.TUESDAY,
    DayOfWeek.WEDNESDAY,
    DayOfWeek.THURSDAY,
    DayOfWeek.FRIDAY,
    DayOfWeek.SATURDAY,
    DayOfWeek.SUNDAY,
)

private val SnoozeChoices = listOf(1, 5, 10, 15)

private fun shortDay(day: DayOfWeek): String = when (day) {
    DayOfWeek.MONDAY -> "M"
    DayOfWeek.TUESDAY -> "T"
    DayOfWeek.WEDNESDAY -> "W"
    DayOfWeek.THURSDAY -> "T"
    DayOfWeek.FRIDAY -> "F"
    DayOfWeek.SATURDAY -> "S"
    DayOfWeek.SUNDAY -> "S"
}

/**
 * Compute the next epoch-millis for which the alarm should fire, and wrap
 * it into an [Alarm] domain object.
 *
 * For one-shot alarms we take today's HH:MM, and push to tomorrow if it has
 * already passed. For repeating alarms we take the nearest future weekday
 * in [repeatDays] (the system layer is responsible for re-arming later).
 *
 * [existingId] is reused on edits so Room upserts rather than inserts a
 * duplicate. For create we mint a new id from the current epoch millis —
 * ids are used as the `AlarmManager` request code.
 */
private fun buildAlarm(
    existingId: Long?,
    hour: Int,
    minute: Int,
    label: String,
    repeatDays: Set<DayOfWeek>,
    difficulty: DifficultyLevel,
    snoozeMinutes: Int,
    sound: String,
    enabled: Boolean,
): Alarm {
    val zone = ZoneId.systemDefault()
    val now = LocalDateTime.now(zone)
    val today = now.toLocalDate()
    val targetTime = LocalTime.of(hour, minute)

    val nextDate: LocalDate = if (repeatDays.isEmpty()) {
        if (targetTime.isAfter(now.toLocalTime())) today else today.plusDays(1)
    } else {
        val todayIsEligible = today.dayOfWeek in repeatDays && targetTime.isAfter(now.toLocalTime())
        if (todayIsEligible) {
            today
        } else {
            // Earliest future day in repeatDays, searching forward from tomorrow.
            (1..7).asSequence()
                .map { today.plusDays(it.toLong()) }
                .first { it.dayOfWeek in repeatDays }
        }
    }
    val triggerAtEpochMillis = LocalDateTime.of(nextDate, targetTime)
        .atZone(zone)
        .toInstant()
        .toEpochMilli()

    val id = existingId ?: System.currentTimeMillis()

    return Alarm(
        id = id,
        triggerAtEpochMillis = triggerAtEpochMillis,
        label = label.trim(),
        enabled = enabled,
        difficulty = difficulty,
        snoozeMinutes = snoozeMinutes,
        sound = sound,
        repeatDays = repeatDays,
    )
}
