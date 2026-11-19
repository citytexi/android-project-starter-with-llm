package com.example.starter.konsist

import org.junit.Test
import kotlin.test.assertTrue

/**
 * Design system components live in `component.<kind>`, take taps only through `component.pressable`,
 * and use only non-interactive Material 3 parts.
 */
class ComponentRulesTest {
    private fun componentFiles() = allProductionFiles().filter { isInPackage(it.packagee?.name.orEmpty(), COMPONENT_PACKAGE) }

    @Test
    fun `components live in a kind subpackage`() {
        val files = componentFiles()
        assertTrue(files.isNotEmpty(), "no design system components found, scope is wrong")
        val violations = files.filter { it.packagee?.name == COMPONENT_PACKAGE }.map { it.projectPath }
        assertTrue(
            violations.isEmpty(),
            "put components in $COMPONENT_PACKAGE.<kind>, not the component package itself:\n${violations.joinToString("\n")}",
        )
    }

    @Test
    fun `only the pressable package handles taps`() {
        val files = componentFiles().filterNot { isInPackage(it.packagee?.name.orEmpty(), PRESSABLE_PACKAGE) }
        assertTrue(files.isNotEmpty(), "no design system components outside pressable found, scope is wrong")
        // A qualified call skips the import, so the call text is checked too.
        val call = Regex("""\b(${TAP_CALLS.joinToString("|")})\s*[({]""")
        val violations =
            files.flatMap { file ->
                file.imports.filter { it.name in TAP_IMPORTS }.map { "${file.projectPath}: ${it.name}" } +
                    call.findAll(file.text).map { "${file.projectPath}: ${it.value}" }
            }
        assertTrue(
            violations.isEmpty(),
            "build taps on StarterPressableSurface instead of handling them directly:\n${violations.joinToString("\n")}",
        )
    }

    @Test
    fun `components use only non-interactive Material 3 parts`() {
        val files = componentFiles()
        assertTrue(files.isNotEmpty(), "no design system components found, scope is wrong")
        // Material 3 Surface(onClick), IconButton, FilterChip and friends bring their own ripple and press handling.
        val violations =
            files.flatMap { file ->
                file.imports
                    .filter { isInPackage(it.name, "androidx.compose.material3") && it.name !in MATERIAL_ALLOWED }
                    .map { "${file.projectPath}: ${it.name}" }
            }
        assertTrue(
            violations.isEmpty(),
            "components may import only ${MATERIAL_ALLOWED.joinToString()} from Material 3:\n${violations.joinToString("\n")}",
        )
    }

    private companion object {
        const val COMPONENT_PACKAGE = "com.example.starter.core.designsystem.component"
        const val PRESSABLE_PACKAGE = "$COMPONENT_PACKAGE.pressable"
        val TAP_CALLS =
            listOf(
                "clickable",
                "combinedClickable",
                "toggleable",
                "triStateToggleable",
                "selectable",
                "indication",
                "ripple",
                "pointerInput",
                "detectTapGestures",
            )
        val TAP_IMPORTS =
            setOf(
                "androidx.compose.foundation.clickable",
                "androidx.compose.foundation.combinedClickable",
                "androidx.compose.foundation.indication",
                "androidx.compose.foundation.selection.toggleable",
                "androidx.compose.foundation.selection.triStateToggleable",
                "androidx.compose.foundation.selection.selectable",
                "androidx.compose.material3.ripple",
                "androidx.compose.material.ripple.rememberRipple",
                "androidx.compose.ui.input.pointer.pointerInput",
                "androidx.compose.foundation.gestures.detectTapGestures",
                "androidx.compose.ui.semantics.onClick",
            )
        val MATERIAL_ALLOWED =
            setOf(
                "androidx.compose.material3.Icon",
                "androidx.compose.material3.Text",
                "androidx.compose.material3.CircularProgressIndicator",
                "androidx.compose.material3.LinearProgressIndicator",
            )
    }
}
