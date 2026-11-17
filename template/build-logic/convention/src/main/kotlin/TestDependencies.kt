import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/** Unit-test baseline shared by every Android and JVM module. */
internal fun Project.addUnitTestDependencies() {
    dependencies {
        testImplementation(libs.junit4)
        testImplementation(libs.kotlin.test)
        testImplementation(libs.kotlinx.coroutines.test)
        testImplementation(libs.mockk)
        testImplementation(libs.turbine)
    }
}

/** Instrumented-test baseline for Android modules. Only compiled by verifyStarter. */
internal fun Project.addAndroidTestDependencies() {
    dependencies {
        androidTestImplementation(libs.junit4)
        androidTestImplementation(libs.androidx.test.core)
        androidTestImplementation(libs.androidx.test.runner)
        androidTestImplementation(libs.androidx.test.rules)
        androidTestImplementation(libs.androidx.test.ext.junit)
    }
}
