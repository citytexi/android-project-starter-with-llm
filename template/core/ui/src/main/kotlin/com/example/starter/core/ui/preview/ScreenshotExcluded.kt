package com.example.starter.core.ui.preview

/**
 * Keeps a preview out of the automatic screenshot capture (`starter.android.screenshot`).
 *
 * For previews Robolectric cannot render (Hilt, ViewModel) or that differ on every run (time, random).
 * The build logic refers to this class by its fully qualified name (`ScreenshotExclusionRulesTest` checks the two match).
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
annotation class ScreenshotExcluded
