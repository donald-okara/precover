// Top-level build file where you can add configuration options common to all sub-projects/modules.

val projectVersion = project.findProperty("precover.version")?.toString() ?: "0.1.0-SNAPSHOT"
val isPrecoverEnabled = project.findProperty("precover.enabled")?.toString() != "false"

buildscript {
    val projectVersion = project.findProperty("precover.version")?.toString() ?: "0.1.0-SNAPSHOT"
    val isEnabled = project.findProperty("precover.enabled")?.toString() != "false"

    repositories {
        mavenLocal()
        google()
        mavenCentral()
    }
    dependencies {
        if (isEnabled) {
            classpath("io.github.donald-okara:gradle-plugin:$projectVersion")
        }
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.google.devtools.ksp) apply false
    alias(libs.plugins.jetbrains.kotlin.plugin.serialization) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.spotless)
    alias(libs.plugins.vanniktech.maven.publish) apply false
}

// Load credentials from local.properties if they exist
val localProperties =
    java.util.Properties().apply {
        val file = rootProject.file("local.properties")
        if (file.exists()) {
            file.inputStream().use { load(it) }
        }
    }

allprojects {
    version = projectVersion

    // Map local.properties to the expected Vanniktech property names
    localProperties.forEach { (key, value) ->
        val stringKey = key.toString()
        val stringValue = value.toString()
        when (stringKey) {
            "ossrhUsername" -> {
                extra.set("mavenCentralUsername", stringValue)
            }

            "ossrhPassword" -> {
                extra.set("mavenCentralPassword", stringValue)
            }

            "signingKey" -> {
                val decodedKey =
                    String(
                        java.util.Base64
                            .getDecoder()
                            .decode(stringValue.trim()),
                    )
                extra.set("signingInMemoryKey", decodedKey)
            }

            "signingPassword" -> {
                extra.set("signingInMemoryKeyPassword", stringValue)
            }
        }
    }
}

subprojects {
    plugins.withType<com.vanniktech.maven.publish.MavenPublishPlugin> {
        configure<com.vanniktech.maven.publish.MavenPublishBaseExtension> {
            coordinates(
                groupId = "io.github.donald-okara",
                artifactId = project.name,
                version = projectVersion,
            )

            pom {
                inceptionYear.set("2026")
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
    }
}

tasks.register("printVersion") {
    val versionProvider = provider { project.version.toString() }
    doLast {
        println("Project Version: ${versionProvider.get()}")
    }
}

if (isPrecoverEnabled) {
    apply(plugin = "io.github.donald-okara.precover.root")

    extensions.configure<io.github.donald_okara.precover.gradle.PrecoverRootExtension>("precoverRoot") {
        aggregateCoverageThreshold.set(80f)
    }
}

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
