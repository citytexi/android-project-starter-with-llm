package com.example.starter.konsist

import org.junit.Test
import kotlin.test.assertTrue

class LayerImportRulesTest {
    @Test
    fun `feature does not import data or core network database datastore packages`() {
        val files = productionFiles("feature")
        assertTrue(files.isNotEmpty(), "no feature sources found, scope is wrong")
        val forbidden =
            listOf(
                "com.example.starter.data.",
                "com.example.starter.core.network.",
                "com.example.starter.core.database.",
                "com.example.starter.core.datastore.",
            )
        val violations =
            files.flatMap { file ->
                file.imports
                    .filter { import -> forbidden.any { import.name.startsWith(it) } }
                    .map { "${file.projectPath}: ${it.name}" }
            }
        assertTrue(violations.isEmpty(), "feature imports forbidden layers:\n${violations.joinToString("\n")}")
    }

    @Test
    fun `domain does not import android or androidx`() {
        val files = productionFiles("domain")
        assertTrue(files.isNotEmpty(), "no domain sources found, scope is wrong")
        val violations =
            files.flatMap { file ->
                file.imports
                    .filter { it.name.startsWith("android.") || it.name.startsWith("androidx.") }
                    .map { "${file.projectPath}: ${it.name}" }
            }
        assertTrue(violations.isEmpty(), "domain must stay Android-free:\n${violations.joinToString("\n")}")
    }
}
