pluginManagement {
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
        // 🔥 Add JitPack here for plugins if needed (optional)
        maven { url = uri("https://jitpack.io") }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // 🔥 Add JitPack here to resolve app dependencies
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "HomePageWaterUsage1"
include(":app")
