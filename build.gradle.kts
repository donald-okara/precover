// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        mavenLocal()
        google()
        mavenCentral()
    }
    dependencies {
        if (project.findProperty("precover.enabled") != "false") {
            classpath("io.github.donald-okara:gradle-plugin:1.0.0")
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

val gitVersion: Provider<String> =
    providers
        .exec {
            commandLine("git", "describe", "--tags", "--always", "--dirty")
        }.standardOutput.asText
        .map { it.trim().removePrefix("v") }

allprojects {
    version = gitVersion.getOrElse("0.1.0-SNAPSHOT")
}

tasks.register("printVersion") {
    val versionProvider = provider { project.version.toString() }
    doLast {
        println("Project Version: ${versionProvider.get()}")
    }
}

// Load credentials from local.properties if they exist
val localProperties =
    java.util.Properties().apply {
        val file = rootProject.file("local.properties")
        if (file.exists()) {
            file.inputStream().use { load(it) }
        }
    }

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

if (project.findProperty("precover.enabled") != "false") {
    apply(plugin = "io.github.donald-okara.precover.root")

    val extension = extensions.getByName("precoverRoot")
    try {
        val method = extension::class.java.getMethod("getAggregateCoverageThreshold")
        val property = method.invoke(extension) as org.gradle.api.provider.Property<Float>
        property.set(80f)
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
