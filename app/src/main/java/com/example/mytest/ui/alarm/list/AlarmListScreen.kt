package com.example.mytest.ui.alarm.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mytest.domain.model.Alarm
import com.example.mytest.ui.alarm.AlarmViewModel
import com.example.mytest.ui.common.AxAlarmRow
import com.example.mytest.ui.theme.AlarmXTheme

/**
 * §8.5 wireframe — top bar "Alarms", `LazyColumn` of [AxAlarmRow], FAB.
 *
 * The "+" FAB is wired to a quick-add (now + 1 minute) so the dismiss flow
 * can be exercised without a dedicated CreateAlarm screen. Replace the FAB
 * action with navigation to a CreateAlarmScreen once that screen lands.
 */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun AlarmListScreen(
    viewModel: AlarmViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = AlarmXTheme.colors

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.surfaceBase,
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Alarms", style = AlarmXTheme.typography.titleLg)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.surfaceBase,
                    titleContentColor = colors.textPrimary,
                ),
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { quickAddAlarm(viewModel) },
                shape = CircleShape,
                containerColor = colors.accentBlue,
                contentColor = colors.surfaceBase,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp,
                    focusedElevation = 0.dp,
                    hoveredElevation = 0.dp,
                ),
            ) {
                Text(text = "+", style = AlarmXTheme.typography.titleLg)
            }
        },
    ) { padding ->
        if (state.alarms.isEmpty()) {
            EmptyState(modifier = Modifier.padding(padding))
        } else {
            AlarmList(
                alarms = state.alarms,
                onToggle = viewModel::toggleAlarm,
                // No CreateAlarm/Edit screen yet — tapping a row test-triggers
                // the dismiss flow so the full UX can be exercised. Replace
                // with navigation to an Edit screen once that lands.
                onClick = viewModel::onAlarmTriggered,
                contentPadding = padding,
            )
        }
    }
}

@Composable
private fun AlarmList(
    alarms: List<Alarm>,
    onToggle: (Long, Boolean) -> Unit,
    onClick: (Long) -> Unit,
    contentPadding: PaddingValues,
) {
    val colors = AlarmXTheme.colors
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = contentPadding,
    ) {
        items(items = alarms, key = { it.id }) { alarm ->
            AxAlarmRow(
                alarm = alarm,
                onToggle = { enabled -> onToggle(alarm.id, enabled) },
                onClick = { onClick(alarm.id) },
            )
            // Hairline separator (§8 design rule).
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
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(AlarmXTheme.colors.surfaceRaised),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "+",
                style = AlarmXTheme.typography.displayLg,
                color = AlarmXTheme.colors.textTertiary,
            )
        }
        Text(
            text = "No alarms yet",
            style = AlarmXTheme.typography.titleMd,
            color = AlarmXTheme.colors.textPrimary,
            modifier = Modifier.padding(top = 16.dp),
        )
        Text(
            text = "Tap + to add your first alarm.",
            style = AlarmXTheme.typography.bodyMd,
            color = AlarmXTheme.colors.textSecondary,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

/**
 * Prototype-only. Inserts an alarm that fires in one minute so the Dismiss
 * flow can be exercised. Will be replaced by navigation to a
 * CreateAlarmScreen.
 */
private fun quickAddAlarm(viewModel: AlarmViewModel) {
    val now = System.currentTimeMillis()
    viewModel.createAlarm(
        id = now,
        triggerAtEpochMillis = now + 60_000,
        label = "Quick alarm",
    )
}
