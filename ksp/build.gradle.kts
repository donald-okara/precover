plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.jetbrains.kotlin.plugin.serialization)
    alias(libs.plugins.vanniktech.maven.publish)
}

mavenPublishing {
    pom {
        name.set("Precover KSP")
        description.set("KSP processor for Precover.")
    }
}

dependencies {
    implementation(project(":core"))
    implementation(libs.google.devtools.ksp.api)
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.kotlinx.serialization.json)
}
