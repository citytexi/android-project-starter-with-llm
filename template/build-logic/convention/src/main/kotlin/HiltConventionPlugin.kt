import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/** `starter.hilt`: Hilt + KSP for Android modules, including Hilt test support. */
class HiltConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        check(pluginManager.hasPlugin("com.android.base")) {
            "starter.hilt needs starter.android.application or starter.android.library first"
        }
        pluginManager.apply(libs.plugins.ksp.get().pluginId)
        pluginManager.apply(libs.plugins.hilt.get().pluginId)
        dependencies {
            implementation(libs.hilt.android)
            ksp(libs.hilt.compiler)

            testImplementation(libs.hilt.android.testing)
            kspTest(libs.hilt.compiler)
            androidTestImplementation(libs.hilt.android.testing)
            kspAndroidTest(libs.hilt.compiler)
        }
    }
}
