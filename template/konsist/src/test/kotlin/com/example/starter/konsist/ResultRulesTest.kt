package com.example.starter.konsist

import org.junit.Test
import kotlin.test.assertTrue

/**
 * Data flows Repository -> UseCase -> ViewModel. Repositories throw, UseCases return `Result<T>`
 * (built with `runSuspendCatching`), ViewModels call only UseCases. A UseCase that only delegates
 * is still required.
 */
class ResultRulesTest {
    private fun check(
        message: String,
        violations: List<String>,
    ) = assertTrue(violations.isEmpty(), "$message:\n${violations.joinToString("\n")}")

    private val domainClasses = productionFiles("domain").flatMap { it.classes(includeNested = true) }

    @Test
    fun `suspend UseCase invoke returns Result`() {
        val invokes =
            domainClasses
                .filter { it.name.endsWith("UseCase") }
                .flatMap { uc -> uc.functions().filter { it.name == "invoke" && it.hasSuspendModifier }.map { uc to it } }
        assertTrue(invokes.isNotEmpty(), "no suspend UseCase found, scope is wrong")
        check(
            "suspend operator fun invoke must declare a Result<T> return type (use runSuspendCatching)",
            invokes
                .filterNot { (_, fn) -> fn.returnType?.text?.startsWith("Result<") == true }
                .map { (uc, _) -> "${uc.containingFile.projectPath}: ${uc.name}.invoke" },
        )
    }

    @Test
    fun `Repository functions do not return Result`() {
        val repos =
            productionFiles("domain")
                .flatMap { it.interfaces(includeNested = true) }
                .filter { it.name.endsWith("Repository") }
        assertTrue(repos.isNotEmpty(), "no Repository interfaces found, scope is wrong")
        check(
            "Repository functions throw on failure; wrap them in a UseCase instead of returning Result",
            repos.flatMap { repo ->
                repo
                    .functions()
                    .filter { it.returnType?.text?.startsWith("Result<") == true }
                    .map { "${repo.containingFile.projectPath}: ${repo.name}.${it.name}" }
            },
        )
    }

    @Test
    fun `ViewModels do not inject Repositories`() {
        val viewModels =
            allProductionFiles()
                .flatMap { it.classes(includeNested = true) }
                .filter { cls -> cls.parents().any { it.name == "ViewModel" } }
        assertTrue(viewModels.isNotEmpty(), "no ViewModels found, scope is wrong")
        check(
            "ViewModels must call UseCases, not Repositories (add a UseCase even if it only delegates)",
            viewModels.flatMap { vm ->
                (listOfNotNull(vm.primaryConstructor) + vm.secondaryConstructors)
                    .flatMap { it.parameters }
                    .filter { it.type.name.endsWith("Repository") }
                    .map { "${vm.containingFile.projectPath}: ${vm.name}(${it.name})" }
            },
        )
    }
}
