plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.jetbrains.kotlin.plugin.serialization)
    alias(libs.plugins.vanniktech.maven.publish)
}

mavenPublishing {
    coordinates(
        groupId = "io.github.donald-okara",
        artifactId = "ksp",
        version = project.version.toString()
    )

    pom {
        name.set("Precover KSP")
        description.set("KSP processor for Precover.")
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

    publishToMavenCentral(com.vanniktech.maven.publish.SonatypeHost.S01)
    signAllPublications()
}

dependencies {
    implementation(project(":core"))
    implementation(libs.google.devtools.ksp.api)
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.kotlinx.serialization.json)
}
