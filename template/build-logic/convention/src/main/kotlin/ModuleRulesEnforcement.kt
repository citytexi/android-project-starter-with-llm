import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ProjectDependency

private const val ENFORCED_FLAG = "starter.moduleRules.enforced"

/**
 * Fails the build when a non-test configuration of this project declares a project dependency
 * that [ModuleRules] does not allow. Safe to call several times; the check is registered once.
 *
 * - Every configuration is read except the test ones ([ModuleRules.isTestConfiguration]), so a
 *   custom configuration (`val extra by configurations.creating`) is checked too.
 * - `allDependencies` includes the `extendsFrom` closure: `implementation.extendsFrom(extra)`
 *   is caught on `implementation` and on the classpaths that extend it.
 * - The check runs in `gradle.projectsEvaluated`, after every `afterEvaluate` block of every
 *   module, so dependencies added late in a module's own build file are caught as well.
 */
internal fun Project.enforceModuleRules() {
    if (extensions.extraProperties.has(ENFORCED_FLAG)) return
    extensions.extraProperties.set(ENFORCED_FLAG, true)

    val project = this
    gradle.projectsEvaluated { project.checkModuleRules() }
}

private fun Project.checkModuleRules() {
    val checked = configurations.filterNot { ModuleRules.isTestConfiguration(it.name) }

    // Configurations that reach each forbidden module, directly or through extendsFrom.
    fun forbidden(deps: (Configuration) -> Iterable<ProjectDependency>) =
        checked
            .flatMap { configuration ->
                deps(configuration).filterNot { ModuleRules.isAllowed(path, it.path) }.map { it.path to configuration.name }
            }.groupBy({ it.first }, { it.second })

    val reached = forbidden { it.allDependencies.withType(ProjectDependency::class.java) }
    if (reached.isEmpty()) return
    val declared = forbidden { it.dependencies.withType(ProjectDependency::class.java) }

    val lines =
        reached.keys.sorted().map { target ->
            // Name where the dependency is written; fall back to the configurations that inherit it.
            val where = (declared[target] ?: reached.getValue(target)).distinct().sorted()
            "  - $target (in ${where.joinToString(", ") { "'$it'" }})"
        }
    throw GradleException(
        "Module $path must not depend on:\n" + lines.joinToString("\n") + "\nSee the dependency table in AGENTS.md §2.",
    )
}
