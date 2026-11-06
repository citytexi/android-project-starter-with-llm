import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/** `starter.android.library`: com.android.library with the shared SDK, JVM 17 and module rules. */
class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply(libs.plugins.android.library.get().pluginId)
        extensions.configure<LibraryExtension> { configureAndroidLibrary(this) }
        addUnitTestDependencies()
        addAndroidTestDependencies()
        enforceModuleRules()
    }
}
