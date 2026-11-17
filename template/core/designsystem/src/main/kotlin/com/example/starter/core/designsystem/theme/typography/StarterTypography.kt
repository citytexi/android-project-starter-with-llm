package com.example.starter.core.designsystem.theme.typography

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp

/**
 * Text tokens. `t1`..`t14` is the size scale, the suffix is the weight.
 * Uses the system font. To add a brand font, put it in `res/font` and change [AppFontFamily].
 */
@Immutable
data class StarterTypography(
    val t1Regular: TextStyle,
    val t1Medium: TextStyle,
    val t1Bold: TextStyle,
    val t2Regular: TextStyle,
    val t2Medium: TextStyle,
    val t2Bold: TextStyle,
    val t3Regular: TextStyle,
    val t3Medium: TextStyle,
    val t3Bold: TextStyle,
    val t4Regular: TextStyle,
    val t4Medium: TextStyle,
    val t4Bold: TextStyle,
    val t5Regular: TextStyle,
    val t5Medium: TextStyle,
    val t5Bold: TextStyle,
    val t6Regular: TextStyle,
    val t6Medium: TextStyle,
    val t6Bold: TextStyle,
    val t7Regular: TextStyle,
    val t7Medium: TextStyle,
    val t7Bold: TextStyle,
    val t8Regular: TextStyle,
    val t8Medium: TextStyle,
    val t8Bold: TextStyle,
    val t9Regular: TextStyle,
    val t9Medium: TextStyle,
    val t9Bold: TextStyle,
    val t10Regular: TextStyle,
    val t10Medium: TextStyle,
    val t10Bold: TextStyle,
    val t11Regular: TextStyle,
    val t11Medium: TextStyle,
    val t11Bold: TextStyle,
    val t12Regular: TextStyle,
    val t12Medium: TextStyle,
    val t12Bold: TextStyle,
    val t13Regular: TextStyle,
    val t13Medium: TextStyle,
    val t13Bold: TextStyle,
    val t14Regular: TextStyle,
    val t14Medium: TextStyle,
    val t14Bold: TextStyle,
)

/** The one font family every token uses. */
internal val AppFontFamily: FontFamily = FontFamily.Default

private fun style(
    size: Int,
    lineHeight: Int,
    weight: FontWeight,
) = TextStyle(
    fontFamily = AppFontFamily,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    platformStyle = PlatformTextStyle(includeFontPadding = false),
    lineHeightStyle =
        LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Center,
            trim = LineHeightStyle.Trim.None,
        ),
)

internal val DefaultStarterTypography =
    StarterTypography(
        t1Regular = style(size = 11, lineHeight = 16, weight = FontWeight.W400),
        t1Medium = style(size = 11, lineHeight = 16, weight = FontWeight.W500),
        t1Bold = style(size = 11, lineHeight = 16, weight = FontWeight.W700),
        t2Regular = style(size = 12, lineHeight = 16, weight = FontWeight.W400),
        t2Medium = style(size = 12, lineHeight = 16, weight = FontWeight.W500),
        t2Bold = style(size = 12, lineHeight = 16, weight = FontWeight.W700),
        t3Regular = style(size = 13, lineHeight = 18, weight = FontWeight.W400),
        t3Medium = style(size = 13, lineHeight = 18, weight = FontWeight.W500),
        t3Bold = style(size = 13, lineHeight = 18, weight = FontWeight.W700),
        t4Regular = style(size = 14, lineHeight = 20, weight = FontWeight.W400),
        t4Medium = style(size = 14, lineHeight = 20, weight = FontWeight.W500),
        t4Bold = style(size = 14, lineHeight = 20, weight = FontWeight.W700),
        t5Regular = style(size = 16, lineHeight = 24, weight = FontWeight.W400),
        t5Medium = style(size = 16, lineHeight = 24, weight = FontWeight.W500),
        t5Bold = style(size = 16, lineHeight = 24, weight = FontWeight.W700),
        t6Regular = style(size = 18, lineHeight = 24, weight = FontWeight.W400),
        t6Medium = style(size = 18, lineHeight = 24, weight = FontWeight.W500),
        t6Bold = style(size = 18, lineHeight = 24, weight = FontWeight.W700),
        t7Regular = style(size = 20, lineHeight = 28, weight = FontWeight.W400),
        t7Medium = style(size = 20, lineHeight = 28, weight = FontWeight.W500),
        t7Bold = style(size = 20, lineHeight = 28, weight = FontWeight.W700),
        t8Regular = style(size = 22, lineHeight = 28, weight = FontWeight.W400),
        t8Medium = style(size = 22, lineHeight = 28, weight = FontWeight.W500),
        t8Bold = style(size = 22, lineHeight = 28, weight = FontWeight.W700),
        t9Regular = style(size = 24, lineHeight = 32, weight = FontWeight.W400),
        t9Medium = style(size = 24, lineHeight = 32, weight = FontWeight.W500),
        t9Bold = style(size = 24, lineHeight = 32, weight = FontWeight.W700),
        t10Regular = style(size = 26, lineHeight = 34, weight = FontWeight.W400),
        t10Medium = style(size = 26, lineHeight = 34, weight = FontWeight.W500),
        t10Bold = style(size = 26, lineHeight = 34, weight = FontWeight.W700),
        t11Regular = style(size = 28, lineHeight = 36, weight = FontWeight.W400),
        t11Medium = style(size = 28, lineHeight = 36, weight = FontWeight.W500),
        t11Bold = style(size = 28, lineHeight = 36, weight = FontWeight.W700),
        t12Regular = style(size = 32, lineHeight = 40, weight = FontWeight.W400),
        t12Medium = style(size = 32, lineHeight = 40, weight = FontWeight.W500),
        t12Bold = style(size = 32, lineHeight = 40, weight = FontWeight.W700),
        t13Regular = style(size = 40, lineHeight = 48, weight = FontWeight.W400),
        t13Medium = style(size = 40, lineHeight = 48, weight = FontWeight.W500),
        t13Bold = style(size = 40, lineHeight = 48, weight = FontWeight.W700),
        t14Regular = style(size = 48, lineHeight = 56, weight = FontWeight.W400),
        t14Medium = style(size = 48, lineHeight = 56, weight = FontWeight.W500),
        t14Bold = style(size = 48, lineHeight = 56, weight = FontWeight.W700),
    )
