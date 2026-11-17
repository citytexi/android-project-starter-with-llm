plugins {
    `kotlin-dsl`
}

group = "starter.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    compileOnly(libs.android.gradle.plugin)
    compileOnly(libs.kotlin.gradle.plugin)
    compileOnly(libs.compose.gradle.plugin)
    compileOnly(libs.ksp.gradle.plugin)
    compileOnly(libs.roborazzi.gradle.plugin)
    // The detekt plugin POM leaks gradle-public-api, which is only published on repo.gradle.org.
    compileOnly(libs.detekt.gradle.plugin) {
        exclude(group = "org.gradle.experimental")
    }
    compileOnly(libs.ktlint.gradle.plugin)
    // Exposes the generated `libs` accessors to the convention plugins.
    compileOnly(files((libs as Any).javaClass.superclass.protectionDomain.codeSource.location))

    testImplementation(libs.junit4)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "starter.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = "starter.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("androidCompose") {
            id = "starter.android.compose"
            implementationClass = "AndroidComposeConventionPlugin"
        }
        register("androidFeature") {
            id = "starter.android.feature"
            implementationClass = "AndroidFeatureConventionPlugin"
        }
        register("androidScreenshot") {
            id = "starter.android.screenshot"
            implementationClass = "AndroidScreenshotConventionPlugin"
        }
        register("hilt") {
            id = "starter.hilt"
            implementationClass = "HiltConventionPlugin"
        }
        register("jvmLibrary") {
            id = "starter.jvm.library"
            implementationClass = "JvmLibraryConventionPlugin"
        }
        register("quality") {
            id = "starter.quality"
            implementationClass = "QualityConventionPlugin"
        }
    }
}
