plugins {
    `java-gradle-plugin`
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.jetbrains.kotlin.plugin.serialization)
    alias(libs.plugins.vanniktech.maven.publish)
}

mavenPublishing {
    coordinates(
        groupId = "io.github.donald-okara",
        artifactId = "gradle-plugin",
        version = project.version.toString(),
    )

    pom {
        name.set("Precover Gradle Plugin")
        description.set("Gradle plugin for Precover.")
        inceptionYear.set("2024")
        url.set("https://github.com/donald-okara/precover")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("donald-okara")
                name.set("Donald Isoe")
                url.set("https://github.com/donald-okara")
            }
        }
        scm {
            url.set("https://github.com/donald-okara/precover")
            connection.set("scm:git:git://github.com/donald-okara/precover.git")
            developerConnection.set("scm:git:ssh://git@github.com/donald-okara/precover.git")
        }
    }

    publishToMavenCentral(com.vanniktech.maven.publish.SonatypeHost.CENTRAL_PORTAL)
    if (project.findProperty("signingInMemoryKey") != null) {
        signAllPublications()
    }
}

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
