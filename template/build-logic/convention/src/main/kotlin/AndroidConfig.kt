import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

internal const val ANDROID_JUNIT_RUNNER = "androidx.test.runner.AndroidJUnitRunner"

internal fun Project.configureAndroidApplication(extension: ApplicationExtension) {
    configureAndroidCommon(extension)
    extension.apply {
        defaultConfig {
            targetSdk = libs.versions.targetSdk.get().toInt()
            versionCode = 1
            versionName = "1.0.0"
        }
        buildTypes {
            release {
                isMinifyEnabled = true
                isShrinkResources = true
                proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
                if (file("proguard-rules.pro").exists()) proguardFiles("proguard-rules.pro")
            }
        }
    }
}

internal fun Project.configureAndroidLibrary(extension: LibraryExtension) {
    configureAndroidCommon(extension)
    extension.apply {
        defaultConfig {
            if (file("consumer-rules.pro").exists()) consumerProguardFiles("consumer-rules.pro")
        }
        // Robolectric reads targetSdk from the unit-test manifest. compileSdk (37) is above
        // what Robolectric renders, so library tests run against targetSdk like the app.
        testOptions.targetSdk = libs.versions.targetSdk.get().toInt()
    }
}

private fun Project.configureAndroidCommon(extension: CommonExtension) {
    extension.apply {
        compileSdk = libs.versions.compileSdk.get().toInt()
        defaultConfig.apply {
            minSdk = starterMinSdk
            testInstrumentationRunner = ANDROID_JUNIT_RUNNER
        }
        compileOptions.apply {
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
        }
        testOptions.unitTests.all { test ->
            // The SDK 36 Robolectric sandbox touches jdk.internal.access.SharedSecrets.
            test.jvmArgs("--add-opens=java.base/jdk.internal.access=ALL-UNNAMED")
        }
    }
    extensions.configure<KotlinAndroidProjectExtension> {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }
}
