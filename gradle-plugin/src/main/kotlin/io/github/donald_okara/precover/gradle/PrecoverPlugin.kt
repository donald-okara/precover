package io.github.donald_okara.precover.gradle

import io.github.donald_okara.precover.core.models.RuleType
import io.github.donald_okara.precover.gradle.tasks.PrecoverCheckTask
import io.github.donald_okara.precover.gradle.tasks.PrecoverReportTask
import io.github.donald_okara.precover.gradle.tasks.PrecoverUpdateBaselineTask
import io.github.donald_okara.precover.rules.engine.RuleOverride
import io.github.donald_okara.precover.rules.engine.RuleWeight
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File

/**
 * Analysis plugin for Precover that evaluates Composable preview coverage for a single module.
 *
 * This plugin sets up tasks for generating individual module reports and checking
 * coverage against a threshold. It is typically applied to Android application
 * and library modules.
 */
/**
 * The core Precover plugin that performs analysis on a single Android module.
 *
 * When applied, this plugin:
 * 1. Creates the `precover` extension for configuration.
 * 2. Registers tasks for generating reports and checking coverage.
 * 3. Configures KSP to use the Precover processor.
 *
 * This plugin is typically applied automatically to subprojects by the [PrecoverRootPlugin].
 */
class PrecoverPlugin : Plugin<Project> {
    /**
     * Applies the analysis plugin to the specified [project].
     */
    override fun apply(project: Project) {
        val extension = project.extensions.create("precover", PrecoverExtension::class.java)

        extension.coverageThreshold.convention(80f)
        extension.maxExcludedRatio.convention(1.0f)
        extension.htmlReportEnabled.convention(true)
        extension.jsonReportEnabled.convention(true)
        extension.baselineFile.convention(project.rootProject.layout.projectDirectory.file("precover/baselines.json"))
        extension.useBaseline.convention(true)

        // Pre-populate all rules for easier configuration
        RuleType.entries.forEach { type ->
            extension.rules.maybeCreate(type.name)
        }

        extension.rules.all { rule ->
            rule.enabled.convention(true)
            rule.weight.convention(RuleWeight.MEDIUM)
        }

        val ruleOverridesProvider = project.provider {
            extension.rules.associate {
                it.type to RuleOverride(it.enabled.get(), it.weight.get())
            }
        }

        val metadataFile = project.layout.buildDirectory.file("generated/ksp/debug/resources/io/github/donald_okara/precover/precover-metadata.json")

        val reportTask = project.tasks.register("precoverReport", PrecoverReportTask::class.java) {
            it.group = "verification"
            it.description = "Generates Precover coverage reports"
            it.metadataFile.set(metadataFile)
            it.outputDirectory.set(project.layout.buildDirectory.dir("reports/precover"))
            it.modulePath.set(project.path)
            it.htmlEnabled.set(extension.htmlReportEnabled)
            it.jsonEnabled.set(extension.jsonReportEnabled)
            it.ruleOverrides.set(ruleOverridesProvider)
        }

        project.tasks.register("precoverUpdateBaseline", PrecoverUpdateBaselineTask::class.java) {
            it.group = "verification"
            it.description = "Updates the Precover coverage baseline for this module"
            it.reportFile.set(reportTask.flatMap { report -> report.outputDirectory.file("precover-report.json") })
            it.baselineFile.set(extension.baselineFile)
            it.modulePath.set(project.path)
        }

        project.tasks.register("precoverCheck", PrecoverCheckTask::class.java) {
            it.group = "verification"
            it.description = "Checks Precover coverage against threshold or baseline"
            it.metadataFile.set(metadataFile)
            it.threshold.set(extension.coverageThreshold)
            it.maxExcludedRatio.set(extension.maxExcludedRatio)
            it.ruleOverrides.set(ruleOverridesProvider)
            it.baselineFile.set(extension.baselineFile)
            it.modulePath.set(project.path)
            it.useBaseline.set(extension.useBaseline)
        }

        project.afterEvaluate {
            // Support multiple variants by looking for any KSP Kotlin task
            val kspTasks = project.tasks.matching { it.name.startsWith("ksp") && it.name.endsWith("Kotlin") }
            if (kspTasks.isNotEmpty()) {
                reportTask.configure { it.dependsOn(kspTasks) }
                project.tasks.named("precoverCheck").configure { it.dependsOn(kspTasks) }
                project.tasks.named("precoverUpdateBaseline").configure { it.dependsOn(kspTasks) }

                // If it's a non-debug variant, we might need to adjust metadataFile path
                // But for now, we'll keep the convention or try to find it
            }

            // Link to the standard check task
            project.tasks.findByName("check")?.dependsOn("precoverCheck")
        }
    }
}
