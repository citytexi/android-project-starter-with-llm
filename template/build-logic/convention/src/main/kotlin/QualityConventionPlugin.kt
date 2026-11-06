import dev.detekt.gradle.extensions.DetektExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jlleitschuh.gradle.ktlint.KtlintExtension

/**
 * `starter.quality`: ktlint + detekt. The root build applies it to every module.
 * Rules live in `.editorconfig` (ktlint) and `config/detekt/detekt.yml` (detekt).
 */
class QualityConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply(libs.plugins.ktlint.get().pluginId)
        pluginManager.apply(libs.plugins.detekt.get().pluginId)

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
            source.setFrom(
                "src/main/kotlin",
                "src/main/java",
                "src/test/kotlin",
                "src/test/java",
                "src/androidTest/kotlin",
                "src/androidTest/java",
            )
        }
    }
}
