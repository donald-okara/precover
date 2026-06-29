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
}

apply(plugin = "io.github.donald-okara.precover.root")

configure<PrecoverRootExtension> {
    aggregateCoverageThreshold.set(80f)
}


