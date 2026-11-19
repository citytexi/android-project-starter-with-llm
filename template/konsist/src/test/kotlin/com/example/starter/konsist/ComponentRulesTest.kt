package com.example.starter.konsist

import org.junit.Test
import kotlin.test.assertTrue

/**
 * Design system components live in `component.<kind>`, take taps only through `component.pressable`,
 * and use only non-interactive Material 3 parts. `component.pressable` stays internal, so a component cannot hide in it.
 */
class ComponentRulesTest {
    private fun componentFiles() = allProductionFiles().filter { isInPackage(it.packagee?.name.orEmpty(), COMPONENT_PACKAGE) }

    private fun designSystemFiles() = allProductionFiles().filter { isInPackage(it.packagee?.name.orEmpty(), DESIGN_SYSTEM_PACKAGE) }

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
    fun `the pressable package declares nothing public`() {
        val files = componentFiles().filter { isInPackage(it.packagee?.name.orEmpty(), PRESSABLE_PACKAGE) }
        assertTrue(files.isNotEmpty(), "no pressable files found, scope is wrong")
        // Components are public; keeping pressable internal stops one from moving in to skip the tap rule.
        val violations =
            files.flatMap { file ->
                val declarations =
                    file.classes(includeNested = false).map { it.name to it.hasPublicOrDefaultModifier } +
                        file.objects(includeNested = false).map { it.name to it.hasPublicOrDefaultModifier } +
                        file.interfaces(includeNested = false).map { it.name to it.hasPublicOrDefaultModifier } +
                        file.functions(includeNested = false, includeLocal = false).map { it.name to it.hasPublicOrDefaultModifier } +
                        file.properties(includeNested = false).map { it.name to it.hasPublicOrDefaultModifier }
                declarations.filter { it.second }.map { "${file.projectPath}: ${it.first}" }
            }
        assertTrue(
            violations.isEmpty(),
            "make these internal or private, and put public components in $COMPONENT_PACKAGE.<kind>:\n${violations.joinToString("\n")}",
        )
    }

    @Test
    fun `only the pressable package handles taps`() {
        // The whole design system, not only components, so a component outside the component package is checked too.
        val files = designSystemFiles().filterNot { isInPackage(it.packagee?.name.orEmpty(), PRESSABLE_PACKAGE) }
        assertTrue(files.any { isInPackage(it.packagee?.name.orEmpty(), COMPONENT_PACKAGE) }, "no components found, scope is wrong")
        // A qualified call skips the import, so the call text is checked too, without comments and strings.
        val call = Regex("""\b(${TAP_CALLS.joinToString("|")})\s*[({]""")
        val violations =
            files.flatMap { file ->
                file.imports.filter { it.name in TAP_IMPORTS }.map { "${file.projectPath}: ${it.name}" } +
                    call.findAll(file.text.codeOnly()).map { "${file.projectPath}: ${it.value}" }
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

    /** The text with comments and string literals blanked out, so a word like `toggleable (` in a KDoc does not count as a call. */
    private fun String.codeOnly(): String {
        val out = StringBuilder(length)
        var i = 0
        while (i < length) {
            val end = skippedEnd(i)
            if (end < 0) {
                out.append(this[i])
                i++
            } else {
                out.append(" ")
                i = end
            }
        }
        return out.toString()
    }

    /** End of the comment or literal that starts at [i], or -1 when [i] is code. */
    private fun String.skippedEnd(i: Int): Int =
        when {
            startsWith("//", i) -> endAt(indexOf('\n', i), 0)
            startsWith("/*", i) -> endAt(indexOf("*/", i + 2), 2)
            startsWith("\"\"\"", i) -> endAt(indexOf("\"\"\"", i + 3), 3)
            this[i] == '"' -> stringEnd(i)
            // A char literal such as '"' must not open a string.
            this[i] == '\'' -> endAt(indexOf('\'', i + if (getOrNull(i + 1) == '\\') 3 else 2), 1)
            else -> -1
        }

    private fun String.endAt(
        found: Int,
        tokenLength: Int,
    ): Int = if (found < 0) length else found + tokenLength

    /** Index just past the `"` string literal that opens at [start], honoring backslash escapes. */
    private fun String.stringEnd(start: Int): Int {
        var i = start + 1
        while (i < length && this[i] != '"' && this[i] != '\n') {
            i += if (this[i] == '\\') 2 else 1
        }
        return minOf(i + 1, length)
    }

    private companion object {
        const val DESIGN_SYSTEM_PACKAGE = "com.example.starter.core.designsystem"
        const val COMPONENT_PACKAGE = "$DESIGN_SYSTEM_PACKAGE.component"
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
                "androidx.compose.material3.ProgressIndicatorDefaults",
                "androidx.compose.material3.HorizontalDivider",
                "androidx.compose.material3.VerticalDivider",
                "androidx.compose.material3.LocalContentColor",
            )
    }
}
