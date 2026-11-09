plugins {
    id("starter.jvm.library")
    // FakeSampleRepository lives in src/testFixtures so domain and feature tests share it.
    `java-test-fixtures`
}

dependencies {
    api(project(":core:common"))
    api(libs.kotlinx.coroutines.core)
}
