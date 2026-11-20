package com.example.starter.konsist

import org.junit.Test
import kotlin.test.assertTrue

/** Screens in `feature` and `app` take colors and type from `StarterTheme` tokens, never from Material 3 or raw values. */
class DesignTokenRulesTest {
    private fun screenFiles() = productionFiles("feature") + productionFiles("app")

    @Test
    fun `feature and app do not import MaterialTheme`() {
        val files = screenFiles()
        assertTrue(files.isNotEmpty(), "no feature or app sources found, scope is wrong")
        // An aliased import keeps its full name in KoImportDeclaration.name, so aliases are caught too.
        val violations =
            files.flatMap { file ->
                file.imports
                    .filter { it.name == MATERIAL_THEME }
                    .map { "${file.projectPath}: ${it.name}" }
            }
        assertTrue(violations.isEmpty(), "use StarterTheme instead of MaterialTheme:\n${violations.joinToString("\n")}")
    }

    @Test
    fun `feature and app do not use a fully qualified MaterialTheme`() {
        val files = screenFiles()
        assertTrue(files.isNotEmpty(), "no feature or app sources found, scope is wrong")
        // An import-free use like `androidx.compose.material3.MaterialTheme.colorScheme` skips the import rule.
        val violations = files.filter { QUALIFIED_MATERIAL_THEME.containsMatchIn(it.text.codeOnly()) }.map { it.projectPath }
        assertTrue(violations.isEmpty(), "use StarterTheme, not a qualified MaterialTheme:\n${violations.joinToString("\n")}")
    }

    @Test
    fun `feature and app do not create raw colors`() {
        val files = screenFiles()
        assertTrue(files.isNotEmpty(), "no feature or app sources found, scope is wrong")
        val violations = files.filter { RAW_COLOR.containsMatchIn(it.text.codeOnly()) }.map { it.projectPath }
        assertTrue(violations.isEmpty(), "use color tokens, not Color(...) literals:\n${violations.joinToString("\n")}")
    }

    private companion object {
        const val MATERIAL_THEME = "androidx.compose.material3.MaterialTheme"
        val QUALIFIED_MATERIAL_THEME = Regex("""\bmaterial3\s*\.\s*MaterialTheme\b""")
        val RAW_COLOR = Regex("""\bColor\(\s*(0x|red\s*=)""")
    }
}
