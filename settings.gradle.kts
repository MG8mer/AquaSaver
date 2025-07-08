pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        gradlePluginPortal()
        mavenCentral()
        maven(url = "https://jitpack.io") // optional: only if needed for plugins
    }
    plugins {
        id("com.android.application") version "8.10.1"
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven(url = "https://jitpack.io") // optional: only if using dependencies from JitPack
    }
}

rootProject.name = "AquaSaver"
include(":app")
