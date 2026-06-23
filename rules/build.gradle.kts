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
    implementation(project(":core"))
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.kotlinx.serialization.json)
    testImplementation(libs.junit)
}

extensions.configure<PublishingExtension> {
    publications {
        register<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}
