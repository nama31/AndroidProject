package com.example.mytest.ui.alarm.dismiss

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mytest.domain.model.ArithmeticTask
import com.example.mytest.ui.alarm.AlarmEvent
import com.example.mytest.ui.alarm.AlarmViewModel
import com.example.mytest.ui.common.AxNumberPad
import com.example.mytest.ui.common.AxSecondaryButton
import com.example.mytest.ui.theme.AlarmXTheme
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.filterIsInstance

/**
 * §8.5 wireframe — full-screen dismissal challenge.
 *
 * Layout top to bottom:
 *  - large clock (`displayXl`)
 *  - alarm label (`bodyMd`, secondary text)
 *  - arithmetic prompt (`monoTask`)
 *  - current digit input
 *  - 4×3 [AxNumberPad]
 *  - "Snooze" [AxSecondaryButton]
 *
 * On a wrong answer we shake the prompt block horizontally; the ViewModel
 * regenerates the challenge in the same tick so the user sees a fresh task.
 */
@Composable
fun DismissScreen(
    viewModel: AlarmViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = AlarmXTheme.colors
    val typography = AlarmXTheme.typography

    var input by remember { mutableStateOf("") }
    val canSubmit by remember { derivedStateOf { input.isNotEmpty() } }

    // Reset input whenever the challenge changes (correct, wrong, or first arrival).
    LaunchedEffect(state.activeChallenge) {
        input = ""
    }

    val shake = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        viewModel.events
            .filterIsInstance<AlarmEvent.WrongAnswerFeedback>()
            .collect {
                runShake(shake)
            }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.surfaceBase,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = currentClockText(),
                style = typography.displayXl,
                color = colors.textPrimary,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = state.alarms.firstOrNull { it.id == state.activeAlarmId }?.label
                    ?: "Wake up",
                style = typography.bodyMd,
                color = colors.textSecondary,
            )

            Spacer(modifier = Modifier.height(40.dp))

            ChallengeBlock(
                challenge = state.activeChallenge,
                input = input,
                shakeOffsetDp = shake.value,
            )

            Spacer(modifier = Modifier.weight(1f))

            AxNumberPad(
                onDigit = { d ->
                    if (input.length < MAX_INPUT_LEN) input += d.toString()
                },
                onBackspace = { input = input.dropLast(1) },
                onSubmit = {
                    val parsed = input.toIntOrNull()
                    if (parsed != null) viewModel.submitAnswer(parsed)
                },
                submitEnabled = canSubmit,
            )

            Spacer(modifier = Modifier.height(16.dp))

            AxSecondaryButton(
                text = "Snooze",
                onClick = viewModel::snoozeActiveAlarm,
                modifier = Modifier.fillMaxWidth(),
                enabled = state.preferences.snoozeEnabled,
            )
        }
    }
}

@Composable
private fun ChallengeBlock(
    challenge: ArithmeticTask?,
    input: String,
    shakeOffsetDp: Float,
) {
    val colors = AlarmXTheme.colors
    val typography = AlarmXTheme.typography

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                translationX = shakeOffsetDp * density
            },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = challenge?.prompt ?: "—",
                style = typography.monoTask,
                color = colors.textPrimary,
            )
            Text(
                text = input.ifEmpty { " " }, // keep height stable
                style = typography.titleLg,
                color = colors.accentBlue,
            )
        }
    }
}

private const val MAX_INPUT_LEN = 5

private val clockFormatter = DateTimeFormatter.ofPattern("HH:mm")

private fun currentClockText(): String =
    clockFormatter.format(
        Instant.ofEpochMilli(System.currentTimeMillis())
            .atZone(ZoneId.systemDefault()),
    )

/**
 * Damped horizontal shake: 24dp → -24dp → 16dp → -16dp → 8dp → 0dp.
 * `Animatable<Float>` is in dp-equivalent units; multiplied by `density` in
 * the [graphicsLayer] block.
 */
private suspend fun runShake(shake: Animatable<Float, *>) {
    val keyframes = listOf(24f, -24f, 16f, -16f, 8f, 0f)
    for (target in keyframes) {
        shake.animateTo(target, animationSpec = tween(durationMillis = 60))
    }
}
