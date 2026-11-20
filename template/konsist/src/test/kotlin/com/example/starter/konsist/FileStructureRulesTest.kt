package com.example.starter.konsist

import org.junit.Test
import kotlin.test.assertTrue

/** Each production file holds at most one top-level class, interface or object, so a type is found by its file name. */
class FileStructureRulesTest {
    @Test
    fun `production file declares at most one top-level type`() {
        val files = allProductionFiles()
        assertTrue(files.isNotEmpty(), "no production sources found, scope is wrong")
        val violations =
            files.mapNotNull { file ->
                val types =
                    file.classes(includeNested = false).map { it.name } +
                        file.interfaces(includeNested = false).map { it.name } +
                        file.objects(includeNested = false).map { it.name }
                if (types.size > 1) "${file.projectPath}: ${types.joinToString()}" else null
            }
        assertTrue(
            violations.isEmpty(),
            "one top-level class, interface or object per file; split these:\n${violations.joinToString("\n")}",
        )
    }
}
