package com.example.mytest.ui.alarm.preferences

import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Parcelable
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mytest.domain.model.DifficultyLevel
import com.example.mytest.domain.model.ThemeMode
import com.example.mytest.ui.alarm.AlarmViewModel
import com.example.mytest.ui.common.AxSecondaryButton
import com.example.mytest.ui.common.AxSegmented
import com.example.mytest.ui.common.AxToggle
import com.example.mytest.ui.theme.AlarmXTheme

/**
 * User preferences screen (§8.5). Reads live [UserPreferences] from the
 * shared [AlarmViewModel.state] and commits changes via
 * [AlarmViewModel.updatePreferences].
 *
 * Sections:
 * - Difficulty — default for newly-created alarms + regeneration on wrong
 *   answer in the Dismiss flow.
 * - Snooze — master toggle, default minutes, max snooze count.
 * - Haptics — buzz on wrong answer.
 * - Theme — SYSTEM / LIGHT / DARK override.
 * - Sound — read-only (ringtone picker is a separate task).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreferencesScreen(
    viewModel: AlarmViewModel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val prefs = state.preferences
    val colors = AlarmXTheme.colors
    val typography = AlarmXTheme.typography
    val context = LocalContext.current

    val ringtonePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        val data = result.data ?: return@rememberLauncherForActivityResult
        val picked: Uri? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            data.getParcelableExtra(
                RingtoneManager.EXTRA_RINGTONE_PICKED_URI,
                Uri::class.java,
            )
        } else {
            @Suppress("DEPRECATION")
            data.getParcelableExtra<Parcelable>(
                RingtoneManager.EXTRA_RINGTONE_PICKED_URI,
            ) as? Uri
        }
        // System picker returns null when the user chooses "Default" if we
        // asked it to — we didn't, but be defensive.
        val newSound = picked?.toString() ?: "default"
        viewModel.updatePreferences { it.copy(sound = newSound) }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.surfaceBase,
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Settings", style = typography.titleLg)
                },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Text(text = "‹", style = typography.titleLg, color = colors.textPrimary)
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
            verticalArrangement = Arrangement.spacedBy(28.dp),
        ) {
            Section(title = "Difficulty", hint = "Applied to new alarms and to the dismiss challenge.") {
                AxSegmented(
                    options = DifficultyLevel.values().toList(),
                    selected = prefs.difficulty,
                    onSelect = { level ->
                        viewModel.updatePreferences { it.copy(difficulty = level) }
                    },
                    label = { it.name.lowercase().replaceFirstChar { c -> c.titlecase() } },
                )
            }

            Section(title = "Snooze") {
                ToggleRow(
                    title = "Allow snooze",
                    subtitle = if (prefs.snoozeEnabled) {
                        "${prefs.defaultSnoozeMinutes} minutes · max ${prefs.maxSnoozeCount}×"
                    } else {
                        "Disabled"
                    },
                    checked = prefs.snoozeEnabled,
                    onCheckedChange = { enabled ->
                        viewModel.updatePreferences { it.copy(snoozeEnabled = enabled) }
                    },
                )
                if (prefs.snoozeEnabled) {
                    Text(
                        text = "Default minutes",
                        style = typography.labelSm,
                        color = colors.textSecondary,
                    )
                    AxSegmented(
                        options = SnoozeChoices,
                        selected = prefs.defaultSnoozeMinutes.takeIf { it in SnoozeChoices } ?: 5,
                        onSelect = { minutes ->
                            viewModel.updatePreferences { it.copy(defaultSnoozeMinutes = minutes) }
                        },
                        label = { "$it m" },
                    )
                    Text(
                        text = "Max snooze count",
                        style = typography.labelSm,
                        color = colors.textSecondary,
                    )
                    AxSegmented(
                        options = MaxSnoozeChoices,
                        selected = prefs.maxSnoozeCount.takeIf { it in MaxSnoozeChoices } ?: 3,
                        onSelect = { count ->
                            viewModel.updatePreferences { it.copy(maxSnoozeCount = count) }
                        },
                        label = { "${it}×" },
                    )
                }
            }

            Section(title = "Haptics") {
                ToggleRow(
                    title = "Buzz on wrong answer",
                    subtitle = "Vibrates the device when a challenge answer is rejected.",
                    checked = prefs.hapticsOnWrongAnswer,
                    onCheckedChange = { enabled ->
                        viewModel.updatePreferences { it.copy(hapticsOnWrongAnswer = enabled) }
                    },
                )
            }

            Section(title = "Theme") {
                AxSegmented(
                    options = ThemeMode.values().toList(),
                    selected = prefs.themeMode,
                    onSelect = { mode ->
                        viewModel.updatePreferences { it.copy(themeMode = mode) }
                    },
                    label = { mode ->
                        mode.name.lowercase().replaceFirstChar { c -> c.titlecase() }
                    },
                )
            }

            Section(title = "Sound") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = ringtoneTitle(context, prefs.sound),
                            style = typography.bodyMd,
                            color = colors.textPrimary,
                        )
                        Text(
                            text = "Used for newly-scheduled alarms.",
                            style = typography.labelSm,
                            color = colors.textSecondary,
                        )
                    }
                    AxSecondaryButton(
                        text = "Change",
                        onClick = {
                            val current = parseSoundUri(prefs.sound)
                            val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                                putExtra(
                                    RingtoneManager.EXTRA_RINGTONE_TYPE,
                                    RingtoneManager.TYPE_ALARM,
                                )
                                putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                                // Silent alarms are an anti-pattern — don't expose.
                                putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
                                putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Alarm sound")
                                putExtra(
                                    RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI,
                                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM),
                                )
                                if (current != null) {
                                    putExtra(
                                        RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
                                        current,
                                    )
                                }
                            }
                            ringtonePickerLauncher.launch(intent)
                        },
                    )
                }
            }

            // Hairline at the bottom for visual closure.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(colors.surfaceStroke),
            )
        }
    }
}

@Composable
private fun Section(
    title: String,
    hint: String? = null,
    content: @Composable () -> Unit,
) {
    val colors = AlarmXTheme.colors
    val typography = AlarmXTheme.typography
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = title.uppercase(),
            style = typography.labelSm,
            color = colors.textSecondary,
        )
        content()
        if (hint != null) {
            Text(
                text = hint,
                style = typography.labelSm,
                color = colors.textTertiary,
            )
        }
    }
}

@Composable
private fun ToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val colors = AlarmXTheme.colors
    val typography = AlarmXTheme.typography
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = typography.bodyMd, color = colors.textPrimary)
            Text(text = subtitle, style = typography.labelSm, color = colors.textSecondary)
        }
        AxToggle(checked = checked, onCheckedChange = onCheckedChange)
    }
}

private val SnoozeChoices = listOf(1, 5, 10, 15)
private val MaxSnoozeChoices = listOf(1, 3, 5, 10)

/**
 * Parse a stored sound string into a [Uri]. Returns `null` for the
 * `"default"` sentinel and for blank/unparseable values — the ringtone
 * service treats `null` as "system default alarm".
 */
private fun parseSoundUri(raw: String): Uri? {
    if (raw.isBlank()) return null
    if (raw.equals("default", ignoreCase = true)) return null
    return try {
        Uri.parse(raw)
    } catch (t: Throwable) {
        null
    }
}

/**
 * Human-readable title for the selected ringtone. Falls back to
 * "System default" for the sentinel / unresolvable URIs.
 */
private fun ringtoneTitle(context: android.content.Context, raw: String): String {
    val uri = parseSoundUri(raw) ?: return "System default"
    return try {
        val ringtone = RingtoneManager.getRingtone(context, uri) ?: return "System default"
        ringtone.getTitle(context) ?: "System default"
    } catch (t: Throwable) {
        "System default"
    }
}
