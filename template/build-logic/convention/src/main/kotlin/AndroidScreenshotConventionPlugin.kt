import com.android.build.api.dsl.CommonExtension
import com.github.takahirom.roborazzi.AnnotationFilter
import io.github.takahirom.roborazzi.RoborazziExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType

/**
 * `starter.android.screenshot`: Roborazzi screenshot tests on Robolectric, plus every `@Preview`
 * under the module namespace captured automatically (Roborazzi Compose Preview scan).
 *
 * Robolectric runs on the module's targetSdk (`sdk=36`, `targetSdk` in the catalog): the app
 * sets it in defaultConfig and starter.android.library sets `testOptions.targetSdk`.
 *
 * The generated preview tests run only when the task graph holds a Roborazzi record/compare task
 * (`verifyStarter`, the UI diff workflow), not in `testStarter`. Reference images are never
 * committed; locally the gate only checks that rendering succeeds (`recordRoborazziDebug`).
 */
class AndroidScreenshotConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        check(pluginManager.hasPlugin("com.android.base")) {
            "starter.android.screenshot needs starter.android.application or starter.android.library first"
        }
        pluginManager.apply(libs.plugins.roborazzi.get().pluginId)

        val android = extensions.getByType<CommonExtension>()
        val unitTests = android.testOptions.unitTests
        unitTests.isIncludeAndroidResources = true
        unitTests.all { test ->
            test.systemProperty("robolectric.pixelCopyRenderMode", "hardware")
        }

        extensions.configure<RoborazziExtension> {
            generateComposePreviewRobolectricTests.apply {
                enable.set(true)
                // The module build file sets namespace after this plugin is applied, so read it late.
                packages.set(provider { listOfNotNull(android.namespace) })
                includePrivatePreviews.set(true)
                annotationFilter.set(AnnotationFilter.Exclude(SCREENSHOT_EXCLUDED))
                // Robolectric 4.17 renders up to API 36 (see targetSdk in the catalog).
                robolectricConfig.set(mapOf("sdk" to "[${libs.versions.targetSdk.get()}]"))
            }
        }

        // Rendering every preview is slow, so the fast checks skip it and the completion gate runs it.
        // Decided from the task graph, not the command line: verifyStarter pulls in recordRoborazziDebug
        // next to testDebugUnitTest, and both the unit tests and the previews must run there.
        gradle.taskGraph.whenReady {
            if (allTasks.none { isRoborazziRunTask(it.name) }) {
                tasks.withType<Test>().forEach { test ->
                    test.filter {
                        excludeTestsMatching("$GENERATED_PREVIEW_TESTS*")
                        isFailOnNoMatchingTests = false
                    }
                }
            }
        }

        // With Hilt, AGP pushes the unit-test R jar (from isIncludeAndroidResources) through Hilt's
        // ASM transform into testClassesDirs. A module with no tests yet then fails Gradle 9's
        // "test sources present but no tests discovered" check, so turn that check off here.
        // :feature:sample has tests, but a new feature module (feature convention + screenshot)
        // starts without any and would fail testDebugUnitTest, so this stays.
        pluginManager.withPlugin(libs.plugins.hilt.get().pluginId) {
            tasks.withType<Test>().configureEach { failOnNoDiscoveredTests.set(false) }
        }

        dependencies {
            testImplementation(libs.robolectric)
            testImplementation(libs.roborazzi)
            testImplementation(libs.roborazzi.compose)
            testImplementation(libs.roborazzi.junit.rule)
            // The Roborazzi plugin checks that these are declared on this module directly, so they
            // cannot come in transitively.
            testImplementation(libs.roborazzi.compose.preview.scanner.support)
            testImplementation(libs.composable.preview.scanner.android)
            testImplementation(libs.junit4)
            testImplementation(platform(libs.androidx.compose.bom))
            testImplementation(libs.androidx.compose.ui.test.junit4)
            debugImplementation(libs.androidx.compose.ui.test.manifest)
        }
    }
}

private const val SCREENSHOT_EXCLUDED = "com.example.starter.core.ui.preview.ScreenshotExcluded"
private const val GENERATED_PREVIEW_TESTS = "com.github.takahirom.roborazzi.RoborazziPreviewParameterizedTests"
