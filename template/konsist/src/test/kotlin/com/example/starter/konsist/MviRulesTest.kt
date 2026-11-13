package com.example.starter.konsist

import org.junit.Test
import kotlin.test.assertTrue

class MviRulesTest {
    private val viewModels =
        allProductionFiles()
            .flatMap { it.classes(includeNested = true) }
            .filter { cls -> cls.parents().any { it.name == "ViewModel" } }

    @Test
    fun `MutableMviContainer stays private in ViewModels`() {
        val containers =
            viewModels.flatMap { vm ->
                vm.properties().filter { "MutableMviContainer" in it.text }.map { vm to it }
            }
        assertTrue(containers.isNotEmpty(), "no MutableMviContainer found in ViewModels, scope is wrong")
        val exposed =
            containers
                .filterNot { (_, property) -> property.hasPrivateModifier }
                .map { (vm, property) -> "${vm.containingFile.projectPath}: ${vm.name}.${property.name}" }
        assertTrue(
            exposed.isEmpty(),
            "MutableMviContainer must be private; expose `val container: MviContainer<...>` instead:\n" +
                exposed.joinToString("\n"),
        )
    }
}
