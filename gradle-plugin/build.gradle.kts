plugins {
    `java-gradle-plugin`
    `maven-publish`
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.jetbrains.kotlin.plugin.serialization)
}

group = "io.github.donald-okara"
version = "1.0.0"

gradlePlugin {
    plugins {
        create("precover") {
            id = "io.github.donald-okara.precover"
            implementationClass = "io.github.donald_okara.precover.gradle.PrecoverPlugin"
        }
        create("precoverRoot") {
            id = "io.github.donald-okara.precover.root"
            implementationClass = "io.github.donald_okara.precover.gradle.PrecoverRootPlugin"
        }
    }
}

dependencies {
    implementation(project(":core"))
    implementation(project(":rules"))
    implementation(libs.kotlinx.serialization.json)

    compileOnly("com.android.tools.build:gradle:8.2.0")
    compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin:2.2.10")
    compileOnly("com.google.devtools.ksp:symbol-processing-gradle-plugin:2.3.5")

    testImplementation(libs.junit)
}
