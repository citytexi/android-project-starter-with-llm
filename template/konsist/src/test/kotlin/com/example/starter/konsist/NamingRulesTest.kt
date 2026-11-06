package com.example.starter.konsist

import com.lemonappdev.konsist.api.declaration.KoClassDeclaration
import org.junit.Test
import kotlin.test.assertTrue

class NamingRulesTest {
    private fun classes(): List<KoClassDeclaration> =
        productionScope().files.filter { it.isProduction() }.flatMap { it.classes(includeNested = true) }

    private fun check(
        message: String,
        violations: List<String>,
    ) = assertTrue(violations.isEmpty(), "$message:\n${violations.joinToString("\n")}")

    @Test
    fun `ViewModels live in feature or app and end with ViewModel`() {
        val viewModels =
            classes().filter { cls -> cls.parents().any { it.name == "ViewModel" } || cls.name.endsWith("ViewModel") }
        assertTrue(viewModels.isNotEmpty(), "no ViewModels found, scope is wrong")
        check(
            "ViewModels must be in feature/app and named *ViewModel",
            viewModels
                .filter { it.containingFile.layer() !in setOf("feature", "app") || !it.name.endsWith("ViewModel") }
                .map { "${it.containingFile.projectPath}: ${it.name}" },
        )
    }

    @Test
    fun `Repository interfaces live in domain`() {
        val repos =
            productionScope()
                .files
                .filter { it.isProduction() }
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
        val useCases = classes().filter { it.name.endsWith("UseCase") }
        assertTrue(useCases.isNotEmpty(), "no UseCases found, scope is wrong")
        check(
            "UseCases must be in domain and declare operator fun invoke",
            useCases
                .filter { uc ->
                    val hasInvoke = uc.functions().any { it.name == "invoke" && it.hasOperatorModifier }
                    uc.containingFile.layer() != "domain" || !hasInvoke
                }.map { "${it.containingFile.projectPath}: ${it.name}" },
        )
    }
}
