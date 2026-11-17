package com.example.starter.konsist

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * `starter.android.screenshot` names the preview exclusion annotation as a string, since build-logic cannot
 * depend on `core:ui`. Moving or renaming the annotation would silently stop the exclusion, so tie the two here.
 */
class ScreenshotExclusionRulesTest {
    @Test
    fun `screenshot plugin names the existing exclusion annotation`() {
        val files = allProductionFiles()
        val plugin = files.filter { it.name == "AndroidScreenshotConventionPlugin" }
        assertTrue(plugin.size == 1, "AndroidScreenshotConventionPlugin.kt not found exactly once, scope is wrong")
        val configured =
            EXCLUDED_CONSTANT.find(plugin.single().text)?.groupValues?.get(1)
                ?: error("SCREENSHOT_EXCLUDED constant not found in AndroidScreenshotConventionPlugin.kt")

        val annotations =
            files
                .flatMap { it.classes() }
                .filter { it.hasAnnotationModifier && it.name == "ScreenshotExcluded" }
                .map { it.fullyQualifiedName }
        assertEquals(listOf(configured), annotations)
    }

    private companion object {
        val EXCLUDED_CONSTANT = Regex("""SCREENSHOT_EXCLUDED\s*=\s*"([^"]+)"""")
    }
}
