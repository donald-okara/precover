package io.github.donald_okara.precover.gradle

import io.github.donald_okara.precover.core.models.RuleType
import io.github.donald_okara.precover.gradle.tasks.PrecoverCheckTask
import io.github.donald_okara.precover.gradle.tasks.PrecoverReportTask
import io.github.donald_okara.precover.rules.engine.RuleOverride
import io.github.donald_okara.precover.rules.engine.RuleWeight
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File

class PrecoverPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create("precover", PrecoverExtension::class.java)

        extension.coverageThreshold.convention(80f)
        extension.htmlReportEnabled.convention(true)
        extension.jsonReportEnabled.convention(true)

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

        project.tasks.register("precoverCheck", PrecoverCheckTask::class.java) {
            it.group = "verification"
            it.description = "Checks Precover coverage against threshold"
            it.metadataFile.set(metadataFile)
            it.threshold.set(extension.coverageThreshold)
            it.ruleOverrides.set(ruleOverridesProvider)
        }

        project.afterEvaluate {
            val kspTaskName = "kspDebugKotlin"
            val kspTask = project.tasks.findByName(kspTaskName)
            if (kspTask != null) {
                reportTask.configure { it.dependsOn(kspTask) }
                project.tasks.named("precoverCheck").configure { it.dependsOn(kspTask) }
            }

            // Link to the standard check task
            project.tasks.findByName("check")?.dependsOn("precoverCheck")
        }
    }
}
