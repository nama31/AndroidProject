package com.example.mytest.ui.common

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import com.example.mytest.domain.model.Alarm
import com.example.mytest.ui.theme.AlarmXTheme
import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Single row in the alarm list. Layout per §8.5 wireframes:
 *
 * ```
 *  07:30   Workout                       [▣]
 *  Mon Tue Wed Thu Fri
 * ```
 *
 * Hairline separators are drawn by the parent (the row itself is flat).
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AxAlarmRow(
    alarm: Alarm,
    onToggle: (Boolean) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
) {
    val colors = AlarmXTheme.colors
    val typography = AlarmXTheme.typography

    Column(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            )
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = formatTime(alarm.triggerAtEpochMillis),
                    style = typography.displayLg,
                    color = if (alarm.enabled) colors.textPrimary else colors.textTertiary,
                )
                if (alarm.label.isNotBlank()) {
                    Text(
                        text = alarm.label,
                        style = typography.bodyMd,
                        color = colors.textSecondary,
                    )
                }
            }
            AxToggle(checked = alarm.enabled, onCheckedChange = onToggle)
        }

        if (alarm.repeatDays.isNotEmpty()) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                DayOfWeek.values().forEach { day ->
                    val active = day in alarm.repeatDays
                    DayChip(label = dayLabel(day), active = active)
                }
            }
        }
    }
}

@Composable
private fun DayChip(label: String, active: Boolean) {
    val colors = AlarmXTheme.colors
    val bg = if (active) colors.accentBlueSoft else colors.surfaceRaised
    val fg = if (active) colors.accentBlue else colors.textTertiary

    Box(
        modifier = Modifier
            .size(width = 32.dp, height = 24.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bg),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = label, style = AlarmXTheme.typography.labelSm, color = fg)
    }
}

private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

private fun formatTime(epochMillis: Long): String =
    timeFormatter.format(
        Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()),
    )

private fun dayLabel(day: DayOfWeek): String = when (day) {
    DayOfWeek.MONDAY -> "Mon"
    DayOfWeek.TUESDAY -> "Tue"
    DayOfWeek.WEDNESDAY -> "Wed"
    DayOfWeek.THURSDAY -> "Thu"
    DayOfWeek.FRIDAY -> "Fri"
    DayOfWeek.SATURDAY -> "Sat"
    DayOfWeek.SUNDAY -> "Sun"
}
