pluginManagement {
    repositories {
        google() // Keep this simple to include all Google artifacts
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google() // Keep this simple
        mavenCentral()
        // Some libraries might still be on jcenter, though it's deprecated
        // jcenter()
    }
}

rootProject.name = "WakeUpCallApp"
include(":app")
