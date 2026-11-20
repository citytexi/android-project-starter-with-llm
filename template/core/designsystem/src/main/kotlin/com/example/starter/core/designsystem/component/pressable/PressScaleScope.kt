package com.example.starter.core.designsystem.component.pressable

/** What shrinks while pressed. */
internal enum class PressScaleScope {
    /** The whole surface: shadow, background, border and content. */
    SELF,

    /** Only the content. The surface color and border stay put; the factor still comes from the surface size. */
    CONTENT,
}
