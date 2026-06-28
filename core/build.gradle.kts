import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.jetbrains.kotlin.plugin.serialization)
    `maven-publish`
}

group = "io.github.donald-okara"
version = "1.0.0"

dependencies {
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.kotlinx.serialization.json)
    compileOnly("androidx.compose.ui:ui-tooling-preview-jvmstubs:1.10.2")
}

extensions.configure<PublishingExtension> {
    publications {
        register<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}
