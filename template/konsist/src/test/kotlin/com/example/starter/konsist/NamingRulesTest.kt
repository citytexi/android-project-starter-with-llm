package com.example.starter.konsist

import com.lemonappdev.konsist.api.declaration.KoClassDeclaration
import com.lemonappdev.konsist.api.declaration.KoFileDeclaration
import com.lemonappdev.konsist.api.declaration.KoFunctionDeclaration
import org.junit.Test
import kotlin.test.assertTrue

class NamingRulesTest {
    private fun classes(): List<KoClassDeclaration> = allProductionFiles().flatMap { it.classes(includeNested = true) }

    /** Class, object or interface (including `fun interface`), reduced to what the naming rules read. */
    private class Type(
        val name: String,
        val file: KoFileDeclaration,
        val parentNames: List<String>,
        val functions: List<KoFunctionDeclaration>,
    ) {
        val where get() = "${file.projectPath}: $name"
    }

    private fun types(): List<Type> =
        allProductionFiles().flatMap { file ->
            file.classes(includeNested = true).map { Type(it.name, file, it.parents().map { p -> p.name }, it.functions()) } +
                file.objects(includeNested = true).map { Type(it.name, file, it.parents().map { p -> p.name }, it.functions()) } +
                file.interfaces(includeNested = true).map { Type(it.name, file, it.parents().map { p -> p.name }, it.functions()) }
        }

    private fun check(
        message: String,
        violations: List<String>,
    ) = assertTrue(violations.isEmpty(), "$message:\n${violations.joinToString("\n")}")

    @Test
    fun `ViewModels live in feature or app and end with ViewModel`() {
        val viewModels = types().filter { type -> "ViewModel" in type.parentNames || type.name.endsWith("ViewModel") }
        assertTrue(viewModels.isNotEmpty(), "no ViewModels found, scope is wrong")
        check(
            "ViewModels must be in feature/app and named *ViewModel",
            viewModels
                .filter { it.file.layer() !in setOf("feature", "app") || !it.name.endsWith("ViewModel") }
                .map { it.where },
        )
    }

    @Test
    fun `Repository interfaces live in domain`() {
        val repos =
            allProductionFiles()
                .flatMap { it.interfaces(includeNested = true) }
                .filter { it.name.endsWith("Repository") }
        assertTrue(repos.isNotEmpty(), "no Repository interfaces found, scope is wrong")
        check(
            "Repository interfaces must be in domain",
            repos
                .filter { it.containingFile.layer() != "domain" }
                .map { "${it.containingFile.projectPath}: ${it.name}" },
        )
    }

    @Test
    fun `RepositoryImpl classes live in data and implement a domain Repository`() {
        val impls = classes().filter { it.name.endsWith("RepositoryImpl") }
        assertTrue(impls.isNotEmpty(), "no RepositoryImpl found, scope is wrong")
        // Limitation: Konsist does not resolve the parent's package here, so the parent is matched by
        // simple name against the Repository interfaces declared in domain. A same-named interface
        // elsewhere would be accepted.
        val domainRepos =
            productionFiles("domain")
                .flatMap { it.interfaces(includeNested = true) }
                .filter { it.name.endsWith("Repository") }
                .map { it.name }
                .toSet()
        check(
            "RepositoryImpl must be in data and implement a domain Repository",
            impls
                .filter { impl ->
                    impl.containingFile.layer() != "data" || impl.parents().none { it.name in domainRepos }
                }.map { "${it.containingFile.projectPath}: ${it.name}" },
        )
    }

    @Test
    fun `UseCases live in domain and expose operator invoke`() {
        // Classes, objects and (fun) interfaces all count as UseCases.
        val useCases = types().filter { it.name.endsWith("UseCase") }
        assertTrue(useCases.isNotEmpty(), "no UseCases found, scope is wrong")
        check(
            "UseCases must be in domain and declare operator fun invoke",
            useCases
                .filter { uc ->
                    val hasInvoke = uc.functions.any { it.name == "invoke" && it.hasOperatorModifier }
                    uc.file.layer() != "domain" || !hasInvoke
                }.map { it.where },
        )
    }
}
