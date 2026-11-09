plugins {
    id("starter.jvm.library")
    // Hilt's annotation processor for DispatcherModule. hilt-core is the JVM half of Hilt, so
    // this module stays Android-free.
    alias(libs.plugins.ksp)
}

dependencies {
    api(libs.javax.inject)
    api(libs.kotlinx.coroutines.core)
    implementation(libs.hilt.core)
    ksp(libs.hilt.compiler)
}
