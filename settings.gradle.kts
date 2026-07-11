pluginManagement {
    repositories {
        if (System.getenv("PRECOVER_LOCAL_DEV") != null) {
            mavenLocal()
        }
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        if (System.getenv("PRECOVER_LOCAL_DEV") != null) {
            mavenLocal()
        }
        google()
        mavenCentral()
    }
}

rootProject.name = "precover"
include(":app")
include(":core")
include(":ksp")
include(":rules")
include(":gradle-plugin")
