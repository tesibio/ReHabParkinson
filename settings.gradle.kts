pluginManagement {
    plugins {
        //id("com.android.library") version "8.9.1" apply false
        //id("com.android.application") version "8.9.1" apply false
        id("org.jetbrains.kotlin.android") version "1.9.24" apply false
    }

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

rootProject.name = "ReHabParkinson"

include(":app")
include(":opencv")
project(":opencv").projectDir = File(rootDir, "opencv-4.11.0-android-sdk/OpenCV-android-sdk/sdk")
