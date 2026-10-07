rootProject.name = "RWX"

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://artifacts.consensys.net/public/maven/maven/")
        maven("https://jitpack.io")
        gradlePluginPortal()
    }
}

include(":core")
include(":ui")
include(":mod-api")
include(":mod-tools")
include(":slick2d-lwjgl3")
include(":desktop")
include(":android")
