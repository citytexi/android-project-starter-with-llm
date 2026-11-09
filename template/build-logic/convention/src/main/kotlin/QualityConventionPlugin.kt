import dev.detekt.gradle.extensions.DetektExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jlleitschuh.gradle.ktlint.KtlintExtension

/** Source sets detekt reads: production (main + build types) and every test source set. */
private val DETEKT_SOURCE_SETS =
    listOf("main", "debug", "release", "test", "testDebug", "testRelease", "testFixtures", "androidTest", "androidTestDebug")

/**
 * `starter.quality`: ktlint + detekt + the [ModuleRules] dependency check. The root build applies
 * it to every module, so the dependency check does not depend on which plugins a module uses.
 * Rules live in `.editorconfig` (ktlint) and `config/detekt/detekt.yml` (detekt).
 */
class QualityConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply(libs.plugins.ktlint.get().pluginId)
        pluginManager.apply(libs.plugins.detekt.get().pluginId)
        enforceModuleRules()

        extensions.configure<KtlintExtension> {
            // Skip generated sources (KSP, BuildConfig) under this module's build directory.
            // Match on the build directory itself: the project may live under a path that contains "build".
            val buildDir = layout.buildDirectory
            filter {
                exclude { it.file.toPath().startsWith(buildDir.get().asFile.toPath()) }
            }
        }
        extensions.configure<DetektExtension> {
            buildUponDefaultConfig.set(true)
            parallel.set(true)
            config.setFrom(rootProject.file("config/detekt/detekt.yml"))
            // Directories that do not exist are skipped.
            source.setFrom(DETEKT_SOURCE_SETS.flatMap { listOf("src/$it/kotlin", "src/$it/java") })
        }
    }
}
