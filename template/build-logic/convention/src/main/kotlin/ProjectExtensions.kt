import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.getByType

// The root build applies starter.quality from `subprojects {}`, before the module's own
// `libs` extension exists, so fall back to the root project's catalog (same file).
internal val Project.libs: LibrariesForLibs
    get() = extensions.findByType<LibrariesForLibs>() ?: rootProject.extensions.getByType<LibrariesForLibs>()

/** `starter.minSdk` from gradle.properties. `scripts/generate.py` rewrites it from the PRD. */
internal val Project.starterMinSdk: Int
    get() = providers.gradleProperty("starter.minSdk").get().toInt()

internal fun DependencyHandler.implementation(notation: Any) = add("implementation", notation)

internal fun DependencyHandler.debugImplementation(notation: Any) = add("debugImplementation", notation)

internal fun DependencyHandler.testImplementation(notation: Any) = add("testImplementation", notation)

internal fun DependencyHandler.androidTestImplementation(notation: Any) = add("androidTestImplementation", notation)

internal fun DependencyHandler.ksp(notation: Any) = add("ksp", notation)

internal fun DependencyHandler.kspTest(notation: Any) = add("kspTest", notation)

internal fun DependencyHandler.kspAndroidTest(notation: Any) = add("kspAndroidTest", notation)
