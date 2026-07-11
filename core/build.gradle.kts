plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.jetbrains.kotlin.plugin.serialization)
    alias(libs.plugins.vanniktech.maven.publish)
}

mavenPublishing {
    pom {
        name.set("Precover Core")
        description.set("Core logic for Precover, a code coverage tool.")
    }
}

dependencies {
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.kotlinx.serialization.json)
    compileOnly(libs.androidx.compose.ui.tooling.preview.jvmstubs)
}
