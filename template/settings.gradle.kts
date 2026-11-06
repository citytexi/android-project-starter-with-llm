pluginManagement {
    includeBuild("build-logic")

    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Starter"

include(":app")
include(":feature:sample")
include(":domain")
include(":data")
include(
    ":core:designsystem",
    ":core:ui",
    ":core:navigation",
    ":core:network",
    ":core:database",
    ":core:datastore",
    ":core:common",
    ":core:testing:jvm",
    ":core:testing:android",
)
include(":konsist")
