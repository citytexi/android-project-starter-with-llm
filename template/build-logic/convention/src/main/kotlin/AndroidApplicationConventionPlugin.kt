import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/** `starter.android.application`: com.android.application with the shared SDK, JVM 17 and module rules. */
class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply(libs.plugins.android.application.get().pluginId)
        extensions.configure<ApplicationExtension> { configureAndroidApplication(this) }
        addUnitTestDependencies()
        addAndroidTestDependencies()
        enforceModuleRules()
    }
}
