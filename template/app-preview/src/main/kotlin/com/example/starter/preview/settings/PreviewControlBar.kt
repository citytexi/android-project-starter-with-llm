package com.example.starter.preview.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.starter.core.designsystem.theme.StarterPreviewTheme
import com.example.starter.core.designsystem.theme.StarterTheme
import com.example.starter.core.ui.preview.StarterPreview

/** Theme and font scale switches shown under every preview screen. Rows wrap so 2× text is not cut off. */
@Composable
fun PreviewControlBar(
    themeMode: PreviewThemeMode,
    onThemeModeChange: (PreviewThemeMode) -> Unit,
    fontScale: PreviewFontScale,
    onFontScaleChange: (PreviewFontScale) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth().background(StarterTheme.colors.bg.layerFloating).navigationBarsPadding()) {
        HorizontalDivider(color = StarterTheme.colors.stroke.neutralWeak)
        Column(
            modifier =
                Modifier.padding(
                    horizontal = StarterTheme.dimension.spacingX.globalGutter,
                    vertical = StarterTheme.dimension.x1,
                ),
        ) {
            ChoiceRow(
                label = "테마",
                options = PreviewThemeMode.entries,
                selected = themeMode,
                optionLabel = { it.label },
                onSelect = onThemeModeChange,
            )
            ChoiceRow(
                label = "글자",
                options = PreviewFontScale.entries,
                selected = fontScale,
                optionLabel = { it.label },
                onSelect = onFontScaleChange,
            )
        }
    }
}

private const val CHECK_MARK = "✓"

@Composable
private fun <T> ChoiceRow(
    label: String,
    options: List<T>,
    selected: T,
    optionLabel: (T) -> String,
    onSelect: (T) -> Unit,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(StarterTheme.dimension.spacingX.betweenChips),
        itemVerticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = StarterTheme.typography.t4Bold,
            color = StarterTheme.colors.fg.neutralMuted,
        )
        options.forEach { option ->
            val isSelected = option == selected
            FilterChip(
                selected = isSelected,
                onClick = { onSelect(option) },
                // A check mark so the selected chip reads in dark theme without relying on fill contrast alone.
                leadingIcon =
                    if (isSelected) {
                        { Text(text = CHECK_MARK, style = StarterTheme.typography.t4Bold) }
                    } else {
                        null
                    },
                label = { Text(text = optionLabel(option), style = StarterTheme.typography.t4Medium) },
            )
        }
    }
}

@StarterPreview
@Composable
private fun PreviewControlBarPreview() {
    StarterPreviewTheme {
        PreviewControlBar(
            themeMode = PreviewThemeMode.SYSTEM,
            onThemeModeChange = {},
            fontScale = PreviewFontScale.NORMAL,
            onFontScaleChange = {},
        )
    }
}
