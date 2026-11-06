import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType

/**
 * `starter.android.screenshot`: Roborazzi screenshot tests on Robolectric.
 *
 * Robolectric runs on the module's targetSdk (`sdk=36`, `targetSdk` in the catalog): the app
 * sets it in defaultConfig and starter.android.library sets `testOptions.targetSdk`.
 *
 * Modules without screenshot tests still configure and build: `recordRoborazziDebug` then
 * runs no captures. Reference images are never committed; locally the gate only checks
 * that rendering succeeds (`recordRoborazziDebug`).
 */
class AndroidScreenshotConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        check(pluginManager.hasPlugin("com.android.base")) {
            "starter.android.screenshot needs starter.android.application or starter.android.library first"
        }
        pluginManager.apply(libs.plugins.roborazzi.get().pluginId)

        val unitTests = extensions.findByType<LibraryExtension>()?.testOptions?.unitTests
            ?: extensions.getByType<ApplicationExtension>().testOptions.unitTests
        unitTests.isIncludeAndroidResources = true
        unitTests.all { test ->
            test.systemProperty("robolectric.pixelCopyRenderMode", "hardware")
        }

        // With Hilt, AGP pushes the unit-test R jar (from isIncludeAndroidResources) through Hilt's
        // ASM transform into testClassesDirs. A module with no tests yet then fails Gradle 9's
        // "test sources present but no tests discovered" check, so turn that check off here.
        pluginManager.withPlugin(libs.plugins.hilt.get().pluginId) {
            tasks.withType<Test>().configureEach { failOnNoDiscoveredTests.set(false) }
        }

        dependencies {
            testImplementation(libs.robolectric)
            testImplementation(libs.roborazzi)
            testImplementation(libs.roborazzi.compose)
            testImplementation(libs.roborazzi.junit.rule)
            testImplementation(platform(libs.androidx.compose.bom))
            testImplementation(libs.androidx.compose.ui.test.junit4)
            debugImplementation(libs.androidx.compose.ui.test.manifest)
        }
    }
}
