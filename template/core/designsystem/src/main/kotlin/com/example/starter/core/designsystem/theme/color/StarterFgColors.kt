package com.example.starter.core.designsystem.theme.color

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/** Text and icon roles. `on*Solid` sits on the matching `bg.*Solid`. */
@Immutable
data class StarterFgColors(
    val brand: Color,
    val brandContrast: Color,
    val critical: Color,
    val criticalContrast: Color,
    val disabled: Color,
    val informative: Color,
    val informativeContrast: Color,
    val neutral: Color,
    val neutralInverted: Color,
    val neutralMuted: Color,
    val neutralSubtle: Color,
    val onBrandSolid: Color,
    val onCriticalSolid: Color,
    val onInformativeSolid: Color,
    val onNeutralSolid: Color,
    val onPositiveSolid: Color,
    val onWarningSolid: Color,
    val placeholder: Color,
    val positive: Color,
    val positiveContrast: Color,
    val warning: Color,
    val warningContrast: Color,
)
