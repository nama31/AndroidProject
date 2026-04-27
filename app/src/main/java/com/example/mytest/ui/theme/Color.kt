package com.example.mytest.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

// ---------------------------------------------------------------------------
// Raw palette — §8.1 of alarmx-architecture.md
//
// Rule: blue is the ONLY chromatic colour in the app. Everything else is pure
// greyscale. `state/danger` is reserved strictly for destructive confirmations
// and wrong-answer feedback on the Dismiss screen.
// ---------------------------------------------------------------------------

// Light
internal val SurfaceBaseLight = Color(0xFFFFFFFF)
internal val SurfaceRaisedLight = Color(0xFFF6F7F9)
internal val SurfaceStrokeLight = Color(0xFFE6E8EC)
internal val TextPrimaryLight = Color(0xFF0A0A0B)
internal val TextSecondaryLight = Color(0xFF5A5E66)
internal val TextTertiaryLight = Color(0xFF9096A0)
internal val AccentBlueLight = Color(0xFF2F6BFF)
internal val AccentBlueSoftLight = Color(0xFFE8EEFF)
internal val StateDangerLight = Color(0xFFD93A3A)

// Dark
internal val SurfaceBaseDark = Color(0xFF000000)
internal val SurfaceRaisedDark = Color(0xFF0B0B0E)
internal val SurfaceStrokeDark = Color(0xFF1C1D22)
internal val TextPrimaryDark = Color(0xFFFFFFFF)
internal val TextSecondaryDark = Color(0xFFA0A4AC)
internal val TextTertiaryDark = Color(0xFF6B6F78)
internal val AccentBlueDark = Color(0xFF5B8CFF)
internal val AccentBlueSoftDark = Color(0xFF14204A)
internal val StateDangerDark = Color(0xFFFF6B6B)

// ---------------------------------------------------------------------------
// Semantic colour container
// ---------------------------------------------------------------------------

/**
 * AlarmX semantic colour tokens. Material3's [androidx.compose.material3.ColorScheme]
 * doesn't cleanly express the black/white/blue design system (e.g.
 * "surface/stroke", "accent/blue-soft"), so we expose a parallel container
 * via [LocalAlarmXColors] alongside the Material scheme.
 */
@Immutable
data class AlarmXColors(
    val surfaceBase: Color,
    val surfaceRaised: Color,
    val surfaceStroke: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val accentBlue: Color,
    val accentBlueSoft: Color,
    val stateDanger: Color,
    val isLight: Boolean,
)

val LightAlarmXColors = AlarmXColors(
    surfaceBase = SurfaceBaseLight,
    surfaceRaised = SurfaceRaisedLight,
    surfaceStroke = SurfaceStrokeLight,
    textPrimary = TextPrimaryLight,
    textSecondary = TextSecondaryLight,
    textTertiary = TextTertiaryLight,
    accentBlue = AccentBlueLight,
    accentBlueSoft = AccentBlueSoftLight,
    stateDanger = StateDangerLight,
    isLight = true,
)

val DarkAlarmXColors = AlarmXColors(
    surfaceBase = SurfaceBaseDark,
    surfaceRaised = SurfaceRaisedDark,
    surfaceStroke = SurfaceStrokeDark,
    textPrimary = TextPrimaryDark,
    textSecondary = TextSecondaryDark,
    textTertiary = TextTertiaryDark,
    accentBlue = AccentBlueDark,
    accentBlueSoft = AccentBlueSoftDark,
    stateDanger = StateDangerDark,
    isLight = false,
)

val LocalAlarmXColors = compositionLocalOf<AlarmXColors> {
    error("AlarmXColors not provided. Wrap your content in AlarmXTheme { ... }.")
}
