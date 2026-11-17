import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * `starter.android.feature`: library + compose + hilt, plus the modules every feature uses
 * (`:domain`, `:core:designsystem`, `:core:ui`, `:core:navigation`).
 */
class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("starter.android.library")
        pluginManager.apply("starter.android.compose")
        pluginManager.apply("starter.hilt")
        dependencies {
            implementation(project(":domain"))
            implementation(project(":core:designsystem"))
            implementation(project(":core:ui"))
            implementation(project(":core:navigation"))

            implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.androidx.lifecycle.viewmodel.compose)
        }
    }
}
