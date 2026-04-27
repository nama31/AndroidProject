package com.example.mytest.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.LocalAbsoluteTonalElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.unit.dp

// ---------------------------------------------------------------------------
// AlarmXTheme — §8.3 of alarmx-architecture.md
//
// Design mandate: NO shadows. Material3 colours its surfaces by tonal
// elevation, so we pin absolute tonal elevation to 0.dp — every Surface stays
// flat, separation is expressed via the 1px hairline stroke token
// (surfaceStroke) or by swapping between surfaceBase and surfaceRaised.
//
// Note: dynamic colour is intentionally disabled. AppBlock-style design
// requires a fixed black/white/blue palette regardless of OEM accent.
// ---------------------------------------------------------------------------

private val MaterialLightScheme = lightColorScheme(
    primary = AccentBlueLight,
    onPrimary = SurfaceBaseLight,
    primaryContainer = AccentBlueSoftLight,
    onPrimaryContainer = AccentBlueLight,
    background = SurfaceBaseLight,
    onBackground = TextPrimaryLight,
    surface = SurfaceBaseLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = SurfaceRaisedLight,
    onSurfaceVariant = TextSecondaryLight,
    outline = SurfaceStrokeLight,
    outlineVariant = SurfaceStrokeLight,
    error = StateDangerLight,
    onError = SurfaceBaseLight,
)

private val MaterialDarkScheme = darkColorScheme(
    primary = AccentBlueDark,
    onPrimary = SurfaceBaseLight,
    primaryContainer = AccentBlueSoftDark,
    onPrimaryContainer = AccentBlueDark,
    background = SurfaceBaseDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceBaseDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceRaisedDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = SurfaceStrokeDark,
    outlineVariant = SurfaceStrokeDark,
    error = StateDangerDark,
    onError = SurfaceBaseDark,
)

/**
 * Root theme. Wrap the whole app (`setContent { AlarmXTheme { ... } }`) and
 * read semantic tokens via [AlarmXTheme.colors] / [AlarmXTheme.typography].
 */
@Composable
fun AlarmXTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DarkAlarmXColors else LightAlarmXColors
    val materialScheme = if (darkTheme) MaterialDarkScheme else MaterialLightScheme

    CompositionLocalProvider(
        LocalAlarmXColors provides colors,
        LocalAlarmXTypography provides AlarmXTypographyDefault,
        // Pin tonal elevation to 0.dp so Material surfaces never recolor themselves.
        LocalAbsoluteTonalElevation provides 0.dp,
    ) {
        MaterialTheme(
            colorScheme = materialScheme,
            typography = MaterialTypography,
            content = content,
        )
    }
}

/**
 * Convenience accessor: `AlarmXTheme.colors.accentBlue`,
 * `AlarmXTheme.typography.displayXl`.
 */
object AlarmXTheme {
    val colors: AlarmXColors
        @Composable
        @ReadOnlyComposable
        get() = LocalAlarmXColors.current

    val typography: AlarmXTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalAlarmXTypography.current
}

/**
 * Backwards-compatible alias used by the existing [com.example.mytest.MainActivity].
 * Delegates to [AlarmXTheme]. Prefer calling [AlarmXTheme] directly in new code.
 *
 * `dynamicColor` is accepted for source compatibility but ignored — the design
 * system requires a fixed palette.
 */
@Composable
@Suppress("UNUSED_PARAMETER")
fun MytestTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    AlarmXTheme(darkTheme = darkTheme, content = content)
}
