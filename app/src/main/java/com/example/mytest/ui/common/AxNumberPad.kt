package com.example.mytest.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.border
import com.example.mytest.ui.theme.AlarmXTheme

/**
 * 4×3 numeric keypad used on [com.example.mytest.ui.alarm.dismiss.DismissScreen].
 *
 * Row layout:
 * ```
 *   1  2  3
 *   4  5  6
 *   7  8  9
 *   ⌫  0  ✓
 * ```
 *
 * Keys are flat (no shadow) and use the `surfaceStroke` hairline. The submit
 * key turns into a solid blue tile when [submitEnabled] is `true`.
 */
@Composable
fun AxNumberPad(
    onDigit: (Int) -> Unit,
    onBackspace: () -> Unit,
    onSubmit: () -> Unit,
    submitEnabled: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        DigitsRow(1, 2, 3, onDigit)
        DigitsRow(4, 5, 6, onDigit)
        DigitsRow(7, 8, 9, onDigit)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            PadKey(
                label = "⌫",
                onClick = onBackspace,
                modifier = Modifier.weight(1f),
            )
            PadKey(
                label = "0",
                onClick = { onDigit(0) },
                modifier = Modifier.weight(1f),
            )
            SubmitKey(
                onClick = onSubmit,
                enabled = submitEnabled,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun DigitsRow(a: Int, b: Int, c: Int, onDigit: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        PadKey(label = "$a", onClick = { onDigit(a) }, modifier = Modifier.weight(1f))
        PadKey(label = "$b", onClick = { onDigit(b) }, modifier = Modifier.weight(1f))
        PadKey(label = "$c", onClick = { onDigit(c) }, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun PadKey(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = AlarmXTheme.colors
    Box(
        modifier = modifier
            .aspectRatio(1.6f)
            .clip(RoundedCornerShape(16.dp))
            .border(BorderStroke(1.dp, colors.surfaceStroke), RoundedCornerShape(16.dp))
            .background(colors.surfaceBase)
            .clickable(onClick = onClick)
            .padding(8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = label, style = AlarmXTheme.typography.titleLg, color = colors.textPrimary)
    }
}

@Composable
private fun SubmitKey(onClick: () -> Unit, enabled: Boolean, modifier: Modifier = Modifier) {
    val colors = AlarmXTheme.colors
    val bg = if (enabled) colors.accentBlue else colors.surfaceRaised
    val fg = if (enabled) colors.surfaceBase else colors.textTertiary

    Box(
        modifier = modifier
            .aspectRatio(1.6f)
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = "✓", style = AlarmXTheme.typography.titleLg, color = fg)
    }
}
