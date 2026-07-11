// Top-level build file where you can add configuration options common to all sub-projects/modules.

val projectVersion = project.findProperty("precover.version")?.toString() ?: "1.0.1-SNAPSHOT"
val isPrecoverEnabled = project.findProperty("precover.enabled")?.toString() != "false"

buildscript {
    val projectVersion = project.findProperty("precover.version")?.toString() ?: "1.0.1-SNAPSHOT"
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

    // Map properties (from local.properties or project properties) to the expected Vanniktech property names
    val propertiesToMap =
        listOf(
            "ossrhUsername" to "mavenCentralUsername",
            "ossrhPassword" to "mavenCentralPassword",
            "signingKey" to "signingInMemoryKey",
            "signingPassword" to "signingInMemoryKeyPassword",
        )

    propertiesToMap.forEach { (source, target) ->
        val value =
            project.findProperty(target)?.toString()
                ?: project.findProperty(source)?.toString()
                ?: localProperties.getProperty(source)
                ?: localProperties.getProperty(target)

        if (value != null) {
            if (target == "signingInMemoryKey") {
                try {
                    val decodedKey =
                        String(
                            java.util.Base64
                                .getDecoder()
                                .decode(value.trim()),
                        )
                    extra.set(target, decodedKey)
                } catch (e: Exception) {
                    // If not base64, assume it's the raw key
                    extra.set(target, value)
                }
            } else {
                extra.set(target, value)
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

    val extension = extensions.getByName("precoverRoot")
    try {
        val clz = extension.javaClass
        
        // aggregateCoverageThreshold.set(80f)
        val getThreshold = clz.getMethod("getAggregateCoverageThreshold")
        (getThreshold.invoke(extension) as org.gradle.api.provider.Property<Float>).set(80f)

        // subprojects { ... }
        val subprojectsMethod = clz.getDeclaredMethod("subprojects", org.gradle.api.Action::class.java)
        subprojectsMethod.invoke(extension, object : org.gradle.api.Action<Any> {
            override fun execute(subExtension: Any) {
                try {
                    val subClz = subExtension.javaClass
                    
                    // coverageThreshold.set(80f)
                    (subClz.getMethod("getCoverageThreshold").invoke(subExtension) as org.gradle.api.provider.Property<Float>).set(80f)
                    
                    // maxExcludedRatio.set(0.2f)
                    (subClz.getMethod("getMaxExcludedRatio").invoke(subExtension) as org.gradle.api.provider.Property<Float>).set(0.2f)

                    // PREVIEW_PRESENCE { enable(); mandatory() }
                    subClz.getMethod("PREVIEW_PRESENCE", org.gradle.api.Action::class.java).invoke(subExtension, object : org.gradle.api.Action<Any> {
                        override fun execute(ruleConfig: Any) {
                            val ruleClz = ruleConfig.javaClass
                            ruleClz.getMethod("enable").invoke(ruleConfig)
                            ruleClz.getMethod("mandatory").invoke(ruleConfig)
                        }
                    })

                    // THEME_COVERAGE { disable() }
                    subClz.getMethod("THEME_COVERAGE", org.gradle.api.Action::class.java).invoke(subExtension, object : org.gradle.api.Action<Any> {
                        override fun execute(ruleConfig: Any) {
                            val ruleClz = ruleConfig.javaClass
                            ruleClz.getMethod("disable").invoke(ruleConfig)
                        }
                    })

                    // FONT_SCALE_COVERAGE { enable(); medium() }
                    subClz.getMethod("FONT_SCALE_COVERAGE", org.gradle.api.Action::class.java).invoke(subExtension, object : org.gradle.api.Action<Any> {
                        override fun execute(ruleConfig: Any) {
                            val ruleClz = ruleConfig.javaClass
                            ruleClz.getMethod("enable").invoke(ruleConfig)
                            ruleClz.getMethod("medium").invoke(ruleConfig)
                        }
                    })
                } catch (e: Exception) {
                    // Ignore subproject config errors
                }
            }
        })
    } catch (e: Exception) {
        // Plugin not built yet
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
