import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency

private const val ENFORCED_FLAG = "starter.moduleRules.enforced"

// Main configurations only: implementation, api, compileOnly, runtimeOnly and their
// build-type variants (debugImplementation, releaseApi, ...). Anything with "test" in the
// name (testImplementation, androidTestImplementation, testFixturesApi, ...) is exempt.
private val CHECKED_CONFIGURATION = Regex("^(\\w*?)(implementation|api|compileOnly|runtimeOnly)$", RegexOption.IGNORE_CASE)

private fun isCheckedConfiguration(name: String): Boolean =
    CHECKED_CONFIGURATION.matches(name) && !name.contains("test", ignoreCase = true)

/**
 * Fails the build when a main configuration declares a project dependency that
 * [ModuleRules] does not allow. Safe to call from several convention plugins.
 */
internal fun Project.enforceModuleRules() {
    if (extensions.extraProperties.has(ENFORCED_FLAG)) return
    extensions.extraProperties.set(ENFORCED_FLAG, true)

    afterEvaluate {
        configurations
            .filter { isCheckedConfiguration(it.name) }
            .forEach { configuration ->
                configuration.dependencies.withType(ProjectDependency::class.java).forEach { dependency ->
                    if (!ModuleRules.isAllowed(path, dependency.path)) {
                        throw GradleException("Module $path must not depend on ${dependency.path}")
                    }
                }
            }
    }
}
