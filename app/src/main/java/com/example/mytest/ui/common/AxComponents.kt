package com.example.mytest.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mytest.ui.theme.AlarmXTheme

// ---------------------------------------------------------------------------
// AxComponents — §8.4 of alarmx-architecture.md
//
// This step delivers the three base components:
//   - AxToggle
//   - AxPrimaryButton
//   - AxSecondaryButton
//
// Rules enforced:
//   * No shadows (elevation = 0 on every Material3 component).
//   * Blue is the only accent; every other surface comes from semantic
//     AlarmXColors tokens.
//   * Primary/secondary buttons pin to 56dp tall per spec §8.3.
// ---------------------------------------------------------------------------

/**
 * Binary toggle. Off: neutral greyscale track. On: solid blue track with a
 * white thumb. No gradient, no shadow.
 */
@Composable
fun AxToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = AlarmXTheme.colors
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        enabled = enabled,
        colors = SwitchDefaults.colors(
            // ON: blue track, white thumb.
            checkedThumbColor = colors.surfaceBase,
            checkedTrackColor = colors.accentBlue,
            checkedBorderColor = colors.accentBlue,
            checkedIconColor = colors.accentBlue,
            // OFF: neutral track, primary-coloured thumb for contrast.
            uncheckedThumbColor = colors.textPrimary,
            uncheckedTrackColor = colors.surfaceRaised,
            uncheckedBorderColor = colors.surfaceStroke,
            uncheckedIconColor = colors.surfaceRaised,
            // Disabled: drop to tertiary greyscale — blue is never shown disabled.
            disabledCheckedThumbColor = colors.surfaceBase,
            disabledCheckedTrackColor = colors.textTertiary,
            disabledCheckedBorderColor = colors.textTertiary,
            disabledUncheckedThumbColor = colors.textTertiary,
            disabledUncheckedTrackColor = colors.surfaceRaised,
            disabledUncheckedBorderColor = colors.surfaceStroke,
        ),
    )
}

/**
 * Primary CTA. 56dp tall, 16dp radius, solid blue fill, white label.
 * Disabled state drops to `surfaceRaised` background + `textTertiary` label.
 */
@Composable
fun AxPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = AlarmXTheme.colors
    Button(
        onClick = onClick,
        modifier = modifier
            .defaultMinSize(minHeight = 56.dp)
            .heightIn(min = 56.dp),
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = colors.accentBlue,
            contentColor = colors.surfaceBase,
            disabledContainerColor = colors.surfaceRaised,
            disabledContentColor = colors.textTertiary,
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            focusedElevation = 0.dp,
            hoveredElevation = 0.dp,
            disabledElevation = 0.dp,
        ),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = text, style = AlarmXTheme.typography.titleMd)
        }
    }
}

/**
 * Secondary button. Transparent background, 1px hairline stroke
 * (`surfaceStroke`), primary-coloured label.
 */
@Composable
fun AxSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = AlarmXTheme.colors
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .defaultMinSize(minHeight = 56.dp)
            .heightIn(min = 56.dp),
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(width = 1.dp, color = colors.surfaceStroke),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = colors.surfaceBase,
            contentColor = colors.textPrimary,
            disabledContainerColor = colors.surfaceBase,
            disabledContentColor = colors.textTertiary,
        ),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = text, style = AlarmXTheme.typography.titleMd)
        }
    }
}
