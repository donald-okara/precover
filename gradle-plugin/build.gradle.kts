plugins {
    `java-gradle-plugin`
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.jetbrains.kotlin.plugin.serialization)
    alias(libs.plugins.vanniktech.maven.publish)
}

mavenPublishing {
    pom {
        name.set("Precover Gradle Plugin")
        description.set("Gradle plugin for Precover.")
    }
}

gradlePlugin {
    plugins {
        create("precover") {
            id = "io.github.donald-okara.precover.module"
            implementationClass = "io.github.donald_okara.precover.gradle.PrecoverPlugin"
        }
        create("precoverRoot") {
            id = "io.github.donald-okara.precover"
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

val generatePrecoverVersionProperties = tasks.register("generatePrecoverVersionProperties") {
    val version = project.version.toString()
    val outputDir = layout.buildDirectory.dir("generated/precover")
    val outputFile = outputDir.map { it.file("precover-version.properties") }
    inputs.property("version", version)
    outputs.dir(outputDir)
    doLast {
        outputFile.get().asFile.parentFile.mkdirs()
        outputFile.get().asFile.writeText("version=$version\n")
    }
}

sourceSets.main {
    resources.srcDir(generatePrecoverVersionProperties)
}
