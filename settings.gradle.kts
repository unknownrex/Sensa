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

rootProject.name = "Sensa"

include(":app")

include(":core:domain")
include(":core:database")
include(":core:data")
include(":core:capture")
include(":core:presentation")
include(":core:design-system")

include(":feature:onboarding:domain")
include(":feature:onboarding:data")
include(":feature:onboarding:presentation")

include(":feature:dashboard:presentation")
include(":feature:transactions:presentation")
include(":feature:review:presentation")
include(":feature:settings:presentation")
