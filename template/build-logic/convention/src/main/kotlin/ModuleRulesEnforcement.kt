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

    fun ProjectDependency.requestsTestFixtures() =
        requestedCapabilities.any { ModuleRules.isTestFixturesCapability(it.name) }

    // Configurations that reach each offending module, directly or through extendsFrom.
    fun offending(
        deps: (Configuration) -> Iterable<ProjectDependency>,
        isOffending: (ProjectDependency) -> Boolean,
    ) = checked
        .flatMap { configuration ->
            deps(configuration).filter(isOffending).map { it.path to configuration.name }
        }.groupBy({ it.first }, { it.second })

    val notAllowed: (ProjectDependency) -> Boolean = { !ModuleRules.isAllowed(path, it.path) }
    val allDeps: (Configuration) -> Iterable<ProjectDependency> = { it.allDependencies.withType(ProjectDependency::class.java) }
    val ownDeps: (Configuration) -> Iterable<ProjectDependency> = { it.dependencies.withType(ProjectDependency::class.java) }

    val reached = offending(allDeps, notAllowed)
    val reachedFixtures = offending(allDeps) { it.requestsTestFixtures() }
    if (reached.isEmpty() && reachedFixtures.isEmpty()) return
    val declared = offending(ownDeps, notAllowed)
    val declaredFixtures = offending(ownDeps) { it.requestsTestFixtures() }

    fun lines(
        reached: Map<String, List<String>>,
        declared: Map<String, List<String>>,
        suffix: String,
    ) = reached.keys.sorted().map { target ->
        // Name where the dependency is written; fall back to the configurations that inherit it.
        val where = (declared[target] ?: reached.getValue(target)).distinct().sorted()
        "  - $target$suffix (in ${where.joinToString(", ") { "'$it'" }})"
    }

    val message = buildList {
        if (reached.isNotEmpty()) {
            add("Module $path must not depend on:\n" + lines(reached, declared, "").joinToString("\n"))
        }
        if (reachedFixtures.isNotEmpty()) {
            add(
                "Module $path requests test fixtures outside a test configuration " +
                    "(use testImplementation(testFixtures(...))):\n" +
                    lines(reachedFixtures, declaredFixtures, " test fixtures").joinToString("\n"),
            )
        }
    }
    throw GradleException(message.joinToString("\n") + "\nSee the dependency table in AGENTS.md §2.")
}
