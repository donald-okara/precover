import io.github.donald_okara.precover.gradle.PrecoverRootExtension

// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        mavenLocal()
        google()
        mavenCentral()
    }
    dependencies {
        classpath("io.github.donald-okara:gradle-plugin:1.0.0")
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.google.devtools.ksp) apply false
    alias(libs.plugins.jetbrains.kotlin.plugin.serialization) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.spotless)
}

apply(plugin = "io.github.donald-okara.precover.root")

configure<com.diffplug.gradle.spotless.SpotlessExtension> {
    kotlin {
        target("**/*.kt")
        targetExclude("**/build/**/*.kt")
        ktlint().editorConfigOverride(
            mapOf(
                "ktlint_standard_package-name" to "disabled",
                "ktlint_standard_function-naming" to "disabled",
                "ktlint_standard_no-wildcard-imports" to "disabled",
            ),
        )
    }
    kotlinGradle {
        target("**/*.gradle.kts")
        ktlint()
    }
}

configure<PrecoverRootExtension> {
    aggregateCoverageThreshold.set(80f)
}
