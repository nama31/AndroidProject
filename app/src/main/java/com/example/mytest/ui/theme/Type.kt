package com.example.mytest.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ---------------------------------------------------------------------------
// Typography — §8.2 of alarmx-architecture.md
//
// Default system font family (design spec suggests Inter; swap the FontFamily
// here once the Inter assets are added). Tabular numerics ("tnum") are enabled
// on every style that renders digits — prevents jitter on the alarm clock and
// arithmetic prompts.
// ---------------------------------------------------------------------------

private val DefaultFontFamily = FontFamily.Default
private const val TabularNumerics = "tnum"

/** Custom typography scale that doesn't fit Material3's slot names cleanly. */
@Immutable
data class AlarmXTypography(
    val displayXl: TextStyle,  // alarm time on Dismiss
    val displayLg: TextStyle,  // alarm time in list rows
    val titleLg: TextStyle,    // screen titles
    val titleMd: TextStyle,    // card headers / section labels
    val bodyMd: TextStyle,     // body copy
    val labelSm: TextStyle,    // chips, meta rows
    val monoTask: TextStyle,   // arithmetic prompt
)

val AlarmXTypographyDefault = AlarmXTypography(
    displayXl = TextStyle(
        fontFamily = DefaultFontFamily,
        fontWeight = FontWeight.W500,
        fontSize = 72.sp,
        lineHeight = 80.sp,
        letterSpacing = (-0.5).sp,
        fontFeatureSettings = TabularNumerics,
    ),
    displayLg = TextStyle(
        fontFamily = DefaultFontFamily,
        fontWeight = FontWeight.W500,
        fontSize = 48.sp,
        lineHeight = 56.sp,
        letterSpacing = (-0.25).sp,
        fontFeatureSettings = TabularNumerics,
    ),
    titleLg = TextStyle(
        fontFamily = DefaultFontFamily,
        fontWeight = FontWeight.W600,
        fontSize = 22.sp,
        lineHeight = 28.sp,
    ),
    titleMd = TextStyle(
        fontFamily = DefaultFontFamily,
        fontWeight = FontWeight.W600,
        fontSize = 17.sp,
        lineHeight = 24.sp,
    ),
    bodyMd = TextStyle(
        fontFamily = DefaultFontFamily,
        fontWeight = FontWeight.W400,
        fontSize = 15.sp,
        lineHeight = 22.sp,
    ),
    labelSm = TextStyle(
        fontFamily = DefaultFontFamily,
        fontWeight = FontWeight.W500,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.2.sp,
    ),
    monoTask = TextStyle(
        fontFamily = DefaultFontFamily,
        fontWeight = FontWeight.W500,
        fontSize = 56.sp,
        lineHeight = 64.sp,
        fontFeatureSettings = TabularNumerics,
    ),
)

val LocalAlarmXTypography = compositionLocalOf<AlarmXTypography> {
    error("AlarmXTypography not provided. Wrap your content in AlarmXTheme { ... }.")
}

/**
 * Material3 [Typography] mapping. Consumers that rely on Material slots
 * (e.g. [androidx.compose.material3.Text] defaults) still get sensible styles;
 * everything custom should prefer [LocalAlarmXTypography].
 */
internal val MaterialTypography: Typography = Typography(
    displayLarge = AlarmXTypographyDefault.displayXl,
    displayMedium = AlarmXTypographyDefault.displayLg,
    titleLarge = AlarmXTypographyDefault.titleLg,
    titleMedium = AlarmXTypographyDefault.titleMd,
    bodyMedium = AlarmXTypographyDefault.bodyMd,
    labelSmall = AlarmXTypographyDefault.labelSm,
)

/**
 * Kept for source compatibility with the original Compose template — older
 * call sites referenced `Typography` directly. New code should use
 * [LocalAlarmXTypography] / [AlarmXTheme.typography].
 */
@Deprecated(
    "Use AlarmXTheme.typography or LocalAlarmXTypography instead.",
    ReplaceWith("AlarmXTheme.typography"),
)
val Typography: Typography = MaterialTypography
