package com.example.starter.core.designsystem.component.pressable

/** How a [StarterPressableSurface] with a non-null `selected` is chosen. */
internal enum class PressableSelectionMode {
    /** toggleable, Role.Checkbox. Chips, multi-select boxes. */
    TOGGLE,

    /** selectable, Role.RadioButton. Segment items, single-select boxes. */
    SINGLE,
}
